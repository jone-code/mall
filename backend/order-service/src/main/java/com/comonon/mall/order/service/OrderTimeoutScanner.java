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
 * 兜底超时关单：每 30s 扫描过期未支付订单（替代 MQ 延迟队列，见 order-design.md §1）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScanner {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${mall.order.scan-interval-ms:30000}")
    public void scan() {
        List<OrderEntity> expired = orderMapper.selectList(
                Wrappers.<OrderEntity>lambdaQuery()
                        .eq(OrderEntity::getStatus, OrderStatus.PENDING_PAY)
                        .lt(OrderEntity::getExpireAt, LocalDateTime.now())
                        .last("LIMIT 200"));
        if (expired.isEmpty()) {
            return;
        }
        log.info("timeout scan found {} expired orders", expired.size());
        for (OrderEntity o : expired) {
            try {
                orderService.cancelBySystem(o.getOrderNo());
            } catch (Exception e) {
                log.warn("timeout cancel failed orderNo={}: {}", o.getOrderNo(), e.getMessage());
            }
        }
    }
}
