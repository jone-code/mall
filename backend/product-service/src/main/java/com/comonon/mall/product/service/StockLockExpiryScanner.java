package com.comonon.mall.product.service;

import com.comonon.mall.product.client.OrderClient;
import com.comonon.mall.product.mapper.StockLockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 兜底释放过期库存锁：仅当订单仍为待支付时才释放，避免已支付订单被误释库存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockLockExpiryScanner {

    private static final int BATCH = 100;
    private static final String ORDER_PENDING_PAY = "PENDING_PAY";

    private final StockLockMapper stockLockMapper;
    private final StockService stockService;
    private final OrderClient orderClient;

    @Scheduled(fixedDelayString = "${mall.product.stock-lock-scan-interval-ms:30000}")
    public void scan() {
        LocalDateTime now = LocalDateTime.now();
        List<String> orderNos = stockLockMapper.findExpiredLockedOrderNos(now, BATCH);
        if (orderNos.isEmpty()) {
            return;
        }
        log.info("stock lock expiry scan found {} orders", orderNos.size());
        for (String orderNo : orderNos) {
            try {
                String status = orderClient.getStatus(orderNo);
                if (status == null) {
                    log.warn("skip lock expiry release, order status unknown orderNo={}", orderNo);
                    continue;
                }
                if (!ORDER_PENDING_PAY.equals(status)) {
                    log.debug("skip lock expiry release orderNo={} status={}", orderNo, status);
                    continue;
                }
                stockService.release(orderNo, "LOCK_EXPIRED");
            } catch (Exception e) {
                log.warn("release expired lock failed orderNo={}: {}", orderNo, e.getMessage());
            }
        }
    }
}
