package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.domain.OrderStatus;
import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.entity.OrderItemEntity;
import com.comonon.mall.order.mapper.OrderItemMapper;
import com.comonon.mall.order.vo.FulfillmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 虚拟/服务类订单履约，与卡密/核销码分配同事务提交。
 */
@Service
@RequiredArgsConstructor
public class OrderFulfillmentService {

    private final OrderTxService txService;
    private final OrderItemMapper orderItemMapper;
    private final VirtualCardService virtualCardService;
    private final ServiceVerifyCodeService serviceVerifyCodeService;

    @Transactional
    public void fulfillAfterPaid(OrderEntity order) {
        String type = order.getProductType();
        String orderNo = order.getOrderNo();
        if ("VIRTUAL".equals(type)) {
            if (OrderStatus.COMPLETED.equals(order.getStatus())) {
                return;
            }
            Long spuId = firstSpuId(orderNo);
            FulfillmentVO f = virtualCardService.issueReserved(orderNo, spuId);
            if (f == null) {
                throw BizException.of(ErrorCode.VIRTUAL_CARD_NOT_AVAILABLE,
                        "虚拟商品卡密库存不足，请先在后台录入并绑定该商品的卡密");
            }
            txService.transitionVirtualToCompleted(orderNo, FulfillmentHelper.toJson(f));
        } else if ("SERVICE".equals(type)) {
            if (OrderStatus.SHIPPED.equals(order.getStatus())
                    || OrderStatus.COMPLETED.equals(order.getStatus())) {
                return;
            }
            Long spuId = firstSpuId(orderNo);
            FulfillmentVO f = serviceVerifyCodeService.issueReserved(orderNo, spuId);
            if (f == null) {
                throw BizException.of(ErrorCode.SERVICE_VERIFY_NOT_AVAILABLE,
                        "服务商品核销码库存不足，请先在后台录入并绑定该商品的核销码");
            }
            txService.transitionServiceToShipped(orderNo, FulfillmentHelper.toJson(f));
        }
    }

    private Long firstSpuId(String orderNo) {
        OrderItemEntity item = orderItemMapper.selectOne(
                Wrappers.<OrderItemEntity>lambdaQuery().eq(OrderItemEntity::getOrderNo, orderNo).last("LIMIT 1"));
        return item != null ? item.getSpuId() : null;
    }
}
