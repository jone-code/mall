package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.domain.OrderStatus;
import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.entity.OrderItemEntity;
import com.comonon.mall.order.entity.OrderLogEntity;
import com.comonon.mall.order.mapper.OrderItemMapper;
import com.comonon.mall.order.mapper.OrderLogMapper;
import com.comonon.mall.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 DB 事务操作。状态流转用守卫式原子 UPDATE，避免并发双写。
 */
@Service
@RequiredArgsConstructor
public class OrderTxService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogMapper orderLogMapper;

    @Transactional
    public void persistNewOrder(OrderEntity order, List<OrderItemEntity> items) {
        orderMapper.insert(order);
        for (OrderItemEntity item : items) {
            orderItemMapper.insert(item);
        }
        writeLog(order.getOrderNo(), null, OrderStatus.PENDING_PAY, "USER",
                String.valueOf(order.getUserId()), "创建订单");
    }

    /**
     * 待支付 → 已支付。返回 true 表示本次发生了状态变更。
     * 已 PAID 返回 false（幂等）；非 PENDING_PAY 且非 PAID 抛 ORDER_STATUS_ILLEGAL。
     */
    @Transactional
    public boolean transitionToPaid(String orderNo) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markPaid(orderNo, now);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.PENDING_PAY, OrderStatus.PAID, "SYSTEM", null, "支付成功");
            return true;
        }
        assertCurrent(orderNo, OrderStatus.PAID);
        return false;
    }

    /**
     * 待支付 → 已取消。返回 true 表示本次发生了状态变更。
     * 已 CANCELLED 返回 false（幂等）；非 PENDING_PAY 且非 CANCELLED 抛 ORDER_STATUS_ILLEGAL。
     */
    @Transactional
    public boolean transitionToCancelled(String orderNo, String operatorType, String operatorId, String reason) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markCancelled(orderNo, reason, now);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.PENDING_PAY, OrderStatus.CANCELLED, operatorType, operatorId, reason);
            return true;
        }
        assertCurrent(orderNo, OrderStatus.CANCELLED);
        return false;
    }

    /** 已支付 → 已发货（实物）。 */
    @Transactional
    public void transitionToShipped(String orderNo, String trackingNo, String trackingCompany,
                                    String operatorId) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markShipped(orderNo, now, trackingNo, trackingCompany);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.PAID, OrderStatus.SHIPPED, "ADMIN", operatorId,
                    "发货 " + trackingCompany + " " + trackingNo);
            return;
        }
        assertCurrent(orderNo, OrderStatus.SHIPPED);
    }

    /** 已支付 → 已完成（虚拟）。 */
    @Transactional
    public void transitionVirtualToCompleted(String orderNo, String fulfillmentJson) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markVirtualCompleted(orderNo, now, fulfillmentJson);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.PAID, OrderStatus.COMPLETED, "SYSTEM", null, "虚拟商品自动交付");
            return;
        }
        assertCurrent(orderNo, OrderStatus.COMPLETED);
    }

    /** 已支付 → 已发货（服务，待核销）。 */
    @Transactional
    public void transitionServiceToShipped(String orderNo, String fulfillmentJson) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markServiceShipped(orderNo, now, fulfillmentJson);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.PAID, OrderStatus.SHIPPED, "SYSTEM", null, "服务核销码已生成");
            return;
        }
        assertCurrent(orderNo, OrderStatus.SHIPPED);
    }

    /** 已发货 → 已完成。 */
    @Transactional
    public void transitionToCompleted(String orderNo, String operatorType, String operatorId) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markCompleted(orderNo, now);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.SHIPPED, OrderStatus.COMPLETED, operatorType, operatorId, "确认收货");
            return;
        }
        assertCurrent(orderNo, OrderStatus.COMPLETED);
    }

    /** 申请退款。 */
    @Transactional
    public void transitionToRefunding(String orderNo, String fromStatus, String reason, String operatorId) {
        int n = orderMapper.markRefunding(orderNo, reason, fromStatus);
        if (n > 0) {
            writeLog(orderNo, fromStatus, OrderStatus.REFUNDING, "USER", operatorId, "申请退款: " + reason);
            return;
        }
        assertCurrent(orderNo, OrderStatus.REFUNDING);
    }

    /** 同意退款。 */
    @Transactional
    public void transitionToRefunded(String orderNo, String operatorId) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markRefunded(orderNo, now);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.REFUNDING, OrderStatus.REFUNDED, "ADMIN", operatorId, "退款完成(Mock)");
            return;
        }
        assertCurrent(orderNo, OrderStatus.REFUNDED);
    }

    /** 拒绝退款。 */
    @Transactional
    public void transitionRefundRejected(String orderNo, String restoreStatus, String operatorId) {
        int n = orderMapper.markRefundRejected(orderNo, restoreStatus);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.REFUNDING, restoreStatus, "ADMIN", operatorId, "拒绝退款");
            return;
        }
        OrderEntity o = orderMapper.selectOne(
                Wrappers.<OrderEntity>lambdaQuery().eq(OrderEntity::getOrderNo, orderNo));
        if (o != null && !OrderStatus.REFUNDING.equals(o.getStatus())) {
            return;
        }
        throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "订单状态不允许该操作");
    }

    /** 服务核销完成。 */
    @Transactional
    public void transitionServiceVerified(String orderNo, String operatorId) {
        LocalDateTime now = LocalDateTime.now();
        int n = orderMapper.markServiceVerified(orderNo, now, now);
        if (n > 0) {
            writeLog(orderNo, OrderStatus.SHIPPED, OrderStatus.COMPLETED, "ADMIN", operatorId, "服务核销");
            return;
        }
        assertCurrent(orderNo, OrderStatus.COMPLETED);
    }

    private void assertCurrent(String orderNo, String idempotentStatus) {
        OrderEntity o = orderMapper.selectOne(
                Wrappers.<OrderEntity>lambdaQuery().eq(OrderEntity::getOrderNo, orderNo));
        if (o == null) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!idempotentStatus.equals(o.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "订单状态不允许该操作");
        }
    }

    private void writeLog(String orderNo, String from, String to, String opType, String opId, String remark) {
        OrderLogEntity log = new OrderLogEntity();
        log.setOrderNo(orderNo);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorType(opType);
        log.setOperatorId(opId);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
}
