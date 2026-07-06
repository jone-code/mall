package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.client.CartClient;
import com.comonon.mall.order.client.PayClient;
import com.comonon.mall.order.client.ProductClient;
import com.comonon.mall.order.client.UserClient;
import com.comonon.mall.order.client.dto.AddressSnapshotDto;
import com.comonon.mall.order.client.dto.SkuSnapshotDto;
import com.comonon.mall.order.config.OrderProperties;
import com.comonon.mall.order.domain.OrderStatus;
import com.comonon.mall.order.dto.BatchShipRequest;
import com.comonon.mall.order.dto.CreateOrderRequest;
import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.entity.OrderItemEntity;
import com.comonon.mall.order.mapper.OrderItemMapper;
import com.comonon.mall.order.mapper.OrderLogMapper;
import com.comonon.mall.order.mapper.OrderMapper;
import com.comonon.mall.order.mapper.UserNicknameMapper;
import com.comonon.mall.order.vo.BatchShipResultVO;
import com.comonon.mall.order.vo.CreateOrderResultVO;
import com.comonon.mall.order.vo.LogisticsVO;
import com.comonon.mall.order.vo.OrderItemVO;
import com.comonon.mall.order.vo.OrderLogVO;
import com.comonon.mall.order.vo.OrderStatsVO;
import com.comonon.mall.order.vo.OrderTrendVO;
import com.comonon.mall.order.vo.OpsTodoVO;
import com.comonon.mall.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogMapper orderLogMapper;
    private final OrderTxService txService;
    private final OrderNoGenerator orderNoGenerator;
    private final OrderProperties orderProperties;
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final UserNicknameMapper userNicknameMapper;
    private final VirtualCardService virtualCardService;
    private final ServiceVerifyCodeService serviceVerifyCodeService;
    private final OrderFulfillmentService orderFulfillmentService;
    private final PayClient payClient;
    private final LogisticsMockService logisticsMockService;

    private static final java.util.Set<String> REFUNDABLE = java.util.Set.of(
            OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.COMPLETED);

    // ===================== 创建 =====================

    public CreateOrderResultVO create(Long userId, CreateOrderRequest req) {
        List<long[]> lines = cartClient.selectedLines(userId);
        if (lines.isEmpty()) {
            throw BizException.of(ErrorCode.ORDER_EMPTY_ITEMS, "请先在购物车勾选商品");
        }
        List<Long> skuIds = lines.stream().map(l -> l[0]).toList();
        Map<Long, SkuSnapshotDto> snapshots = productClient.fetchSnapshots(skuIds);

        int ttl = orderProperties.getOrder().getTimeoutSeconds();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusSeconds(ttl);
        String orderNo = orderNoGenerator.next(userId);

        List<OrderItemEntity> items = new ArrayList<>();
        List<Map<String, Object>> lockItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;
        String orderType = null;
        Long primarySpuId = null;

        for (long[] line : lines) {
            long skuId = line[0];
            int qty = (int) line[1];
            SkuSnapshotDto snap = snapshots.get(skuId);
            if (snap == null || !Boolean.TRUE.equals(snap.getSellable())) {
                throw BizException.of(ErrorCode.SKU_NOT_SELLABLE, "商品已下架或不可购买");
            }
            if (snap.getAvailable() == null || snap.getAvailable() < qty) {
                throw BizException.of(ErrorCode.STOCK_LOCK_INSUFFICIENT, "库存不足: " + snap.getSpuTitle());
            }
            BigDecimal subtotal = snap.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);
            itemCount += qty;
            if (orderType == null) {
                orderType = snap.getProductType();
                primarySpuId = snap.getSpuId();
            }

            OrderItemEntity item = new OrderItemEntity();
            item.setOrderNo(orderNo);
            item.setSkuId(skuId);
            item.setSpuId(snap.getSpuId());
            item.setTitle(snap.getSpuTitle());
            item.setSpecText(snap.getSpecText());
            item.setMainImage(snap.getMainImage());
            item.setPrice(snap.getPrice());
            item.setQuantity(qty);
            item.setSubtotal(subtotal);
            item.setProductType(snap.getProductType());
            items.add(item);

            lockItems.add(Map.of("skuId", skuId, "quantity", qty));
        }

        validateOrderLines(lines, snapshots);
        String resolvedType = orderType == null ? "PHYSICAL" : orderType;
        AddressSnapshotDto address = resolveAddress(userId, req.getAddressId(), resolvedType);

        BigDecimal freight = BigDecimal.ZERO;
        BigDecimal payAmount = total.add(freight);

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setTotalAmount(total);
        order.setFreightAmount(freight);
        order.setPayAmount(payAmount);
        order.setItemCount(itemCount);
        order.setProductType(resolvedType);
        order.setReceiver(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setAddressDetail(address.getFullAddress());
        order.setExpireAt(expireAt);
        order.setRemark(req.getRemark());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // 1) 锁库存
        productClient.lock(orderNo, lockItems, ttl);
        // 2) 落库 + 预占码池；失败则补偿释放
        try {
            txService.persistNewOrder(order, items);
            reservePoolIfNeeded(orderNo, resolvedType, primarySpuId);
        } catch (Exception e) {
            log.error("persist order failed, releasing stock orderNo={}", orderNo, e);
            safeRelease(orderNo, "CREATE_FAIL");
            releasePoolReservation(orderNo, resolvedType);
            try {
                txService.transitionToCancelled(orderNo, "SYSTEM", null, "CREATE_FAIL");
            } catch (Exception ignored) {
                // order may not have been persisted
            }
            throw e;
        }
        // 3) 清购物车（尽力而为，失败不影响订单）
        safeRemoveCartLines(userId, skuIds);

        return new CreateOrderResultVO(orderNo, payAmount, expireAt);
    }

    private void safeRemoveCartLines(Long userId, List<Long> skuIds) {
        try {
            cartClient.removeLines(userId, skuIds);
        } catch (Exception e) {
            log.warn("remove cart lines failed userId={} order may exist with cart residue", userId, e);
        }
    }

    // ===================== 取消 / 关单 / 支付 =====================

    /** 用户取消待支付订单。 */
    public void cancelByUser(Long userId, String orderNo) {
        OrderEntity order = getOwned(userId, orderNo);
        txService.transitionToCancelled(orderNo, "USER", String.valueOf(userId), "USER_CANCEL");
        safeRelease(orderNo, "USER_CANCEL");
        releasePoolReservation(orderNo, order.getProductType());
    }

    /** 系统超时关单。 */
    public void cancelBySystem(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        txService.transitionToCancelled(orderNo, "SYSTEM", null, "TIMEOUT");
        safeRelease(orderNo, "TIMEOUT");
        releasePoolReservation(orderNo, order.getProductType());
    }

    /** 运营强制关单。 */
    public void closeByAdmin(String orderNo, String adminId) {
        OrderEntity order = getByNo(orderNo);
        txService.transitionToCancelled(orderNo, "ADMIN", adminId, "ADMIN_CLOSE");
        safeRelease(orderNo, "ADMIN_CLOSE");
        releasePoolReservation(orderNo, order.getProductType());
    }

    /** 支付成功回填（pay-service 内部调用，幂等）。 */
    public void markPaid(String orderNo) {
        boolean changed = txService.transitionToPaid(orderNo);
        if (changed) {
            safeDeduct(orderNo);
            productClient.extendLockForPaid(orderNo);
        }
        OrderEntity order = getByNo(orderNo);
        if (needsAutoFulfill(order)) {
            orderFulfillmentService.fulfillAfterPaid(order);
        }
    }

    /** 兜底重试虚拟/服务履约。 */
    public void retryFulfillment(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        if (!needsAutoFulfill(order)) {
            return;
        }
        orderFulfillmentService.fulfillAfterPaid(order);
    }

    private boolean needsAutoFulfill(OrderEntity order) {
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            return false;
        }
        String type = order.getProductType();
        return "VIRTUAL".equals(type) || "SERVICE".equals(type);
    }

    /** 运营发货（实物）。 */
    public void shipByAdmin(String orderNo, String trackingNo, String trackingCompany, String adminId) {
        OrderEntity order = getByNo(orderNo);
        if (!"PHYSICAL".equals(order.getProductType())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "非实物订单无需发货");
        }
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "仅待发货订单可发货");
        }
        String company = StringUtils.hasText(trackingCompany) ? trackingCompany.trim() : "快递";
        txService.transitionToShipped(orderNo, trackingNo.trim(), company, adminId);
    }

    public BatchShipResultVO batchShipByAdmin(BatchShipRequest req, String adminId) {
        BatchShipResultVO result = new BatchShipResultVO();
        for (BatchShipRequest.Item item : req.getItems()) {
            try {
                shipByAdmin(item.getOrderNo(), item.getTrackingNo(), item.getTrackingCompany(), adminId);
                result.setSuccess(result.getSuccess() + 1);
            } catch (Exception e) {
                result.setFailed(result.getFailed() + 1);
                result.getErrors().add(item.getOrderNo() + ": " + e.getMessage());
            }
        }
        return result;
    }

    /** 用户确认收货。 */
    public void confirmReceive(Long userId, String orderNo) {
        OrderEntity order = getOwned(userId, orderNo);
        if ("SERVICE".equals(order.getProductType())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "服务类订单请到店核销，无需确认收货");
        }
        if (!OrderStatus.SHIPPED.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "当前状态不可确认收货");
        }
        txService.transitionToCompleted(orderNo, "USER", String.valueOf(userId));
    }

    /** 用户申请退款（Mock 审核流：进入 REFUNDING）。 */
    public void applyRefund(Long userId, String orderNo, String reason) {
        OrderEntity order = getOwned(userId, orderNo);
        if (!REFUNDABLE.contains(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "当前状态不可申请退款");
        }
        txService.transitionToRefunding(orderNo, order.getStatus(), reason, String.valueOf(userId));
    }

    /** 运营同意退款（Mock 原路退回）。 */
    public void approveRefundByAdmin(String orderNo, String adminId) {
        OrderEntity order = getByNo(orderNo);
        if (!OrderStatus.REFUNDING.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "仅退款中订单可同意退款");
        }
        payClient.mockRefund(orderNo);
        txService.transitionToRefunded(orderNo, adminId);
        safeRelease(orderNo, "REFUND");
        if ("PHYSICAL".equals(order.getProductType())) {
            productClient.refundRestore(orderNo);
        }
        invalidateFulfillmentAfterRefund(order);
    }

    /** 运营拒绝退款，恢复原状态。 */
    public void rejectRefundByAdmin(String orderNo, String adminId) {
        OrderEntity order = getByNo(orderNo);
        if (!OrderStatus.REFUNDING.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "仅退款中订单可拒绝退款");
        }
        String restore = order.getRefundFromStatus();
        if (restore == null || restore.isBlank()) {
            restore = OrderStatus.PAID;
        }
        txService.transitionRefundRejected(orderNo, restore, adminId);
    }

    /** 运营备注。 */
    public void updateAdminRemark(String orderNo, String remark) {
        getByNo(orderNo);
        orderMapper.updateAdminRemark(orderNo, remark == null ? "" : remark.trim());
    }

    /** 服务核销（Mock）。 */
    public OrderVO verifyService(String verifyCode, String adminId) {
        OrderEntity order = orderMapper.findServiceByVerifyCode(verifyCode.trim());
        if (order == null) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "核销码无效或已使用");
        }
        txService.transitionServiceVerified(order.getOrderNo(), adminId);
        return adminDetail(order.getOrderNo());
    }

    /** Mock 物流轨迹。 */
    public LogisticsVO getLogistics(Long userId, String orderNo) {
        OrderEntity order = getOwned(userId, orderNo);
        LogisticsVO vo = logisticsMockService.build(order);
        if (vo == null) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "暂无物流信息");
        }
        return vo;
    }

    public LogisticsVO adminLogistics(String orderNo) {
        OrderEntity order = getByNo(orderNo);
        LogisticsVO vo = logisticsMockService.build(order);
        if (vo == null) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "暂无物流信息");
        }
        return vo;
    }

    private void safeDeduct(String orderNo) {
        try {
            productClient.deduct(orderNo);
        } catch (Exception e) {
            log.error("deduct stock failed orderNo={}, will retry via scanner", orderNo, e);
        }
    }

    private void safeRelease(String orderNo, String reason) {
        try {
            productClient.release(orderNo, reason);
        } catch (Exception e) {
            log.error("release stock failed orderNo={} reason={}", orderNo, reason, e);
        }
    }

    private void validateOrderLines(List<long[]> lines, Map<Long, SkuSnapshotDto> snapshots) {
        String orderType = null;
        for (long[] line : lines) {
            SkuSnapshotDto snap = snapshots.get(line[0]);
            if (snap == null) {
                continue;
            }
            String pt = snap.getProductType() == null ? "PHYSICAL" : snap.getProductType();
            if (orderType == null) {
                orderType = pt;
            } else if (!orderType.equals(pt)) {
                throw BizException.of(ErrorCode.CART_MIXED_PRODUCT_TYPE,
                        "不可同时结算实物、虚拟、服务类商品，请分开下单");
            }
        }
        if ("VIRTUAL".equals(orderType) || "SERVICE".equals(orderType)) {
            if (lines.size() > 1) {
                throw BizException.of(ErrorCode.BAD_REQUEST, "虚拟/服务类商品请单独购买");
            }
            long[] line = lines.get(0);
            SkuSnapshotDto snap = snapshots.get(line[0]);
            int qty = (int) line[1];
            if (qty > 1) {
                throw BizException.of(ErrorCode.CART_VIRTUAL_QTY_LIMIT, "虚拟/服务类商品每单限购 1 件");
            }
            long pool = "VIRTUAL".equals(orderType)
                    ? virtualCardService.countAvailableBySpu(snap.getSpuId())
                    : serviceVerifyCodeService.countAvailableBySpu(snap.getSpuId());
            if (pool < qty) {
                int code = "VIRTUAL".equals(orderType)
                        ? ErrorCode.VIRTUAL_CARD_NOT_AVAILABLE
                        : ErrorCode.SERVICE_VERIFY_NOT_AVAILABLE;
                String label = "VIRTUAL".equals(orderType) ? "卡密" : "核销码";
                throw BizException.of(code, label + "库存不足，请联系客服或稍后再试");
            }
        }
    }

    private void reservePoolIfNeeded(String orderNo, String productType, Long spuId) {
        if ("VIRTUAL".equals(productType)) {
            virtualCardService.reserveForOrder(orderNo, spuId);
        } else if ("SERVICE".equals(productType)) {
            serviceVerifyCodeService.reserveForOrder(orderNo, spuId);
        }
    }

    private void releasePoolReservation(String orderNo, String productType) {
        if (productType == null) {
            return;
        }
        try {
            if ("VIRTUAL".equals(productType)) {
                virtualCardService.releaseReservation(orderNo);
            } else if ("SERVICE".equals(productType)) {
                serviceVerifyCodeService.releaseReservation(orderNo);
            }
        } catch (Exception e) {
            log.error("release pool reservation failed orderNo={}", orderNo, e);
        }
    }

    private AddressSnapshotDto resolveAddress(Long userId, Long addressId, String productType) {
        if ("VIRTUAL".equals(productType) || "SERVICE".equals(productType)) {
            if (addressId == null) {
                AddressSnapshotDto placeholder = new AddressSnapshotDto();
                placeholder.setReceiver("无需收货");
                placeholder.setPhone("-");
                placeholder.setFullAddress("虚拟/服务类商品无需配送地址");
                return placeholder;
            }
        } else if (addressId == null) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "请选择收货地址");
        }
        return userClient.getAddress(addressId, userId);
    }

    private void invalidateFulfillmentAfterRefund(OrderEntity order) {
        String type = order.getProductType();
        try {
            if ("VIRTUAL".equals(type)) {
                virtualCardService.revokeAfterRefund(order.getOrderNo());
            } else if ("SERVICE".equals(type)) {
                serviceVerifyCodeService.revokeAfterRefund(order.getOrderNo());
            }
        } catch (Exception e) {
            log.error("invalidate fulfillment pool failed orderNo={}", order.getOrderNo(), e);
        }
    }

    // ===================== 查询 =====================

    public List<OrderVO> listByUser(Long userId, String status) {
        var wrapper = Wrappers.<OrderEntity>lambdaQuery()
                .eq(OrderEntity::getUserId, userId)
                .orderByDesc(OrderEntity::getId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderEntity::getStatus, status);
        }
        return orderMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    public com.comonon.mall.common.web.PageResult<OrderVO> listByUserPage(Long userId, String status,
                                                                          int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        var wrapper = Wrappers.<OrderEntity>lambdaQuery()
                .eq(OrderEntity::getUserId, userId)
                .orderByDesc(OrderEntity::getId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderEntity::getStatus, status);
        }
        var pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderEntity>(p, s);
        orderMapper.selectPage(pg, wrapper);
        return com.comonon.mall.common.web.PageResult.of(
                pg.getRecords().stream().map(this::toVO).toList(), p, s, pg.getTotal());
    }

    public OrderVO getDetail(Long userId, String orderNo) {
        return toVO(getOwned(userId, orderNo));
    }

    public com.comonon.mall.common.web.PageResult<OrderVO> adminList(String status, Long userId,
                                                                     String orderNo, String phone,
                                                                     String productType,
                                                                     LocalDate from,
                                                                     LocalDate to,
                                                                     int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        var wrapper = buildAdminQuery(status, userId, orderNo, phone, productType, from, to)
                .orderByDesc(OrderEntity::getId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderEntity> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(p, s);
        orderMapper.selectPage(mpPage, wrapper);
        List<OrderVO> list = mpPage.getRecords().stream().map(this::toVO).toList();
        enrichUserNicknames(list);
        return com.comonon.mall.common.web.PageResult.of(list, p, s, mpPage.getTotal());
    }

    public String adminExportCsv(String status, Long userId, String orderNo, String phone,
                                String productType, LocalDate from, LocalDate to) {
        var wrapper = buildAdminQuery(status, userId, orderNo, phone, productType, from, to)
                .orderByDesc(OrderEntity::getId)
                .last("LIMIT 5000");
        List<OrderVO> list = orderMapper.selectList(wrapper).stream().map(this::toVO).toList();
        enrichUserNicknames(list);
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("订单号,用户ID,类型,状态,应付,件数,收货人,电话,下单时间\n");
        for (OrderVO o : list) {
            sb.append(csv(o.getOrderNo())).append(',')
                    .append(o.getUserId() == null ? "" : o.getUserId()).append(',')
                    .append(csv(o.getProductType())).append(',')
                    .append(csv(o.getStatus())).append(',')
                    .append(o.getPayAmount() == null ? "0" : o.getPayAmount()).append(',')
                    .append(o.getItemCount() == null ? "0" : o.getItemCount()).append(',')
                    .append(csv(o.getReceiver())).append(',')
                    .append(csv(o.getReceiverPhone())).append(',')
                    .append(csv(o.getCreatedAt() == null ? "" : o.getCreatedAt().toString()))
                    .append('\n');
        }
        return sb.toString();
    }

    public OrderVO adminDetail(String orderNo) {
        OrderVO vo = toVO(getByNo(orderNo));
        enrichUserNicknames(List.of(vo));
        vo.setPayment(payClient.getByOrderNo(orderNo));
        vo.setLogs(listOrderLogs(orderNo));
        return vo;
    }

    public OrderTrendVO trend(int days) {
        int d = Math.min(Math.max(days, 1), 30);
        LocalDate start = LocalDate.now().minusDays(d - 1L);
        LocalDateTime startAt = start.atStartOfDay();
        Map<String, Long> orderByDay = new java.util.HashMap<>();
        for (Map<String, Object> row : orderMapper.countOrdersByDay(startAt)) {
            orderByDay.put(String.valueOf(row.get("day")), ((Number) row.get("cnt")).longValue());
        }
        Map<String, BigDecimal> gmvByDay = new java.util.HashMap<>();
        for (Map<String, Object> row : orderMapper.gmvByDay(startAt)) {
            Object gmv = row.get("gmv");
            gmvByDay.put(String.valueOf(row.get("day")),
                    gmv instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) gmv).doubleValue()));
        }
        OrderTrendVO vo = new OrderTrendVO();
        vo.setDays(d);
        for (int i = 0; i < d; i++) {
            LocalDate day = start.plusDays(i);
            String key = day.toString();
            OrderTrendVO.DailyPoint p = new OrderTrendVO.DailyPoint();
            p.setDate(key);
            p.setOrderCount(orderByDay.getOrDefault(key, 0L));
            p.setGmv(gmvByDay.getOrDefault(key, BigDecimal.ZERO));
            vo.getPoints().add(p);
        }
        return vo;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderEntity> buildAdminQuery(
            String status, Long userId, String orderNo, String phone, String productType,
            LocalDate from, LocalDate to) {
        var wrapper = Wrappers.<OrderEntity>lambdaQuery();
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderEntity::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(OrderEntity::getUserId, userId);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(OrderEntity::getOrderNo, orderNo.trim());
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(OrderEntity::getReceiverPhone, phone.trim());
        }
        if (StringUtils.hasText(productType)) {
            wrapper.eq(OrderEntity::getProductType, productType.trim());
        }
        if (from != null) {
            wrapper.ge(OrderEntity::getCreatedAt, from.atStartOfDay());
        }
        if (to != null) {
            wrapper.lt(OrderEntity::getCreatedAt, to.plusDays(1).atStartOfDay());
        }
        return wrapper;
    }

    private List<OrderLogVO> listOrderLogs(String orderNo) {
        return orderLogMapper.selectList(Wrappers.<com.comonon.mall.order.entity.OrderLogEntity>lambdaQuery()
                        .eq(com.comonon.mall.order.entity.OrderLogEntity::getOrderNo, orderNo)
                        .orderByAsc(com.comonon.mall.order.entity.OrderLogEntity::getId))
                .stream().map(OrderLogVO::from).toList();
    }

    private static String csv(String v) {
        if (v == null) return "\"\"";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    /** 主控台统计。 */
    public OrderStatsVO stats() {
        OrderStatsVO vo = new OrderStatsVO();
        long total = 0;
        for (Map<String, Object> row : orderMapper.aggregateByStatus()) {
            String st = String.valueOf(row.get("status"));
            long cnt = row.get("cnt") == null ? 0 : ((Number) row.get("cnt")).longValue();
            total += cnt;
            switch (st) {
                case OrderStatus.PENDING_PAY -> vo.setPendingPay(cnt);
                case OrderStatus.PAID -> vo.setPaid(cnt);
                case OrderStatus.SHIPPED -> vo.setShipped(cnt);
                case OrderStatus.COMPLETED -> vo.setCompleted(cnt);
                case OrderStatus.CANCELLED -> vo.setCancelled(cnt);
                case OrderStatus.REFUNDING -> vo.setRefunding(cnt);
                case OrderStatus.REFUNDED -> vo.setRefunded(cnt);
                default -> { /* ignore unknown status */ }
            }
        }
        vo.setTotalOrders(total);
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        vo.setTodayOrders(orderMapper.countCreatedSince(dayStart));
        BigDecimal todayGmv = orderMapper.gmvPaidSince(dayStart);
        BigDecimal totalGmv = orderMapper.gmvPaidTotal();
        vo.setTodayGmv(todayGmv == null ? BigDecimal.ZERO : todayGmv);
        vo.setTotalGmv(totalGmv == null ? BigDecimal.ZERO : totalGmv);
        return vo;
    }

    /** 运营待办统计。 */
    public OpsTodoVO opsTodos() {
        OrderStatsVO stats = stats();
        OpsTodoVO vo = new OpsTodoVO();
        vo.setPendingPay(stats.getPendingPay());
        vo.setPendingShip(stats.getPaid());
        vo.setRefunding(stats.getRefunding());
        vo.setVirtualCardAvailable(virtualCardService.countAvailable());
        vo.setVirtualPoolEmpty(virtualCardService.countEmptyPools());
        vo.setServicePoolEmpty(serviceVerifyCodeService.countEmptyPools());
        return vo;
    }

    // ===================== 内部辅助 =====================

    public String getStatus(String orderNo) {
        return getByNo(orderNo).getStatus();
    }

    private OrderEntity getByNo(String orderNo) {
        OrderEntity o = orderMapper.selectOne(
                Wrappers.<OrderEntity>lambdaQuery().eq(OrderEntity::getOrderNo, orderNo));
        if (o == null) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return o;
    }

    private OrderEntity getOwned(Long userId, String orderNo) {
        OrderEntity o = getByNo(orderNo);
        if (!userId.equals(o.getUserId())) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return o;
    }

    private OrderVO toVO(OrderEntity o) {
        List<OrderItemVO> items = orderItemMapper.selectList(
                        Wrappers.<OrderItemEntity>lambdaQuery().eq(OrderItemEntity::getOrderNo, o.getOrderNo()))
                .stream().map(OrderItemVO::from).toList();
        return OrderVO.from(o, items);
    }

    private void enrichUserNicknames(List<OrderVO> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Long> userIds = orders.stream()
                .map(OrderVO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, String> nicknames = fetchNicknames(userIds);
        for (OrderVO order : orders) {
            Long uid = order.getUserId();
            if (uid == null) {
                continue;
            }
            String nickname = nicknames.get(uid);
            if (nickname != null && !nickname.isBlank()) {
                order.setUserNickname(nickname);
            }
        }
    }

    private Map<Long, String> fetchNicknames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new java.util.HashMap<>();
        for (Map<String, Object> row : userNicknameMapper.selectNicknamesByIds(userIds)) {
            Object idObj = row.get("id");
            Object nicknameObj = row.get("nickname");
            if (idObj == null || nicknameObj == null) {
                continue;
            }
            long id = idObj instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(idObj));
            String nickname = String.valueOf(nicknameObj);
            if (!nickname.isBlank()) {
                map.put(id, nickname);
            }
        }
        return map;
    }
}
