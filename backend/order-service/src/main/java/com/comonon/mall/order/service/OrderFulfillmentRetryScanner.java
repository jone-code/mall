package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
 * 兜底履约：markPaid 后虚拟/服务自动交付失败时重试（幂等）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFulfillmentRetryScanner {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${mall.order.fulfill-retry-interval-ms:30000}")
    public void scan() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<OrderEntity> stuck = orderMapper.selectList(
                Wrappers.<OrderEntity>lambdaQuery()
                        .eq(OrderEntity::getStatus, OrderStatus.PAID)
                        .in(OrderEntity::getProductType, "VIRTUAL", "SERVICE")
                        .ge(OrderEntity::getPayAt, since)
                        .last("LIMIT 50"));
        for (OrderEntity o : stuck) {
            try {
                orderService.retryFulfillment(o.getOrderNo());
            } catch (Exception e) {
                log.warn("fulfillment retry failed orderNo={}: {}", o.getOrderNo(), e.getMessage());
            }
        }
    }
}
