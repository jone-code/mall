package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.order.client.ProductClient;
import com.comonon.mall.order.domain.OrderStatus;
import com.comonon.mall.order.entity.OrderEntity;
import com.comonon.mall.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 兜底库存扣减：markPaid 后 deduct 失败时，对已支付订单重试扣减（幂等）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStockDeductScanner {

    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Scheduled(fixedDelayString = "${mall.order.deduct-retry-interval-ms:30000}")
    public void scan() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<OrderEntity> paid = orderMapper.selectList(
                Wrappers.<OrderEntity>lambdaQuery()
                        .eq(OrderEntity::getStatus, OrderStatus.PAID)
                        .ge(OrderEntity::getPayAt, since)
                        .last("LIMIT 100"));
        if (paid.isEmpty()) {
            return;
        }
        for (OrderEntity o : paid) {
            try {
                productClient.deduct(o.getOrderNo());
            } catch (Exception e) {
                log.warn("deduct retry failed orderNo={}: {}", o.getOrderNo(), e.getMessage());
            }
        }
    }
}
