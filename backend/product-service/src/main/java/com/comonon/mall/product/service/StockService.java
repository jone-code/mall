package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.dto.StockLockRequest;
import com.comonon.mall.product.entity.SkuStock;
import com.comonon.mall.product.entity.StockFlow;
import com.comonon.mall.product.entity.StockLock;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SkuStockMapper;
import com.comonon.mall.product.mapper.StockFlowMapper;
import com.comonon.mall.product.mapper.StockLockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final SkuStockMapper skuStockMapper;
    private final StockFlowMapper stockFlowMapper;
    private final SkuMapper skuMapper;
    private final StockLockMapper stockLockMapper;

    public void initStock(Long skuId, int available) {
        SkuStock stock = new SkuStock();
        stock.setSkuId(skuId);
        stock.setAvailable(Math.max(available, 0));
        stock.setFrozen(0);
        stock.setVersion(0);
        stock.setUpdatedAt(LocalDateTime.now());
        skuStockMapper.insert(stock);
        if (available > 0) {
            writeFlow(skuId, "ADMIN_SET", available, 0, available, 0, "ADMIN", null);
        }
    }

    public void deleteStock(Long skuId) {
        skuStockMapper.deleteById(skuId);
    }

    public int getAvailable(Long skuId) {
        SkuStock stock = skuStockMapper.selectById(skuId);
        return stock == null || stock.getAvailable() == null ? 0 : stock.getAvailable();
    }

    @Transactional
    public Map<String, Object> setAvailable(Long skuId, int target, String operatorType, String operatorId) {
        if (skuMapper.selectById(skuId) == null) {
            throw BizException.of(ErrorCode.SKU_NOT_FOUND, "SKU 不存在");
        }
        SkuStock stock = skuStockMapper.selectById(skuId);
        if (stock == null) {
            initStock(skuId, target);
            stock = skuStockMapper.selectById(skuId);
        }
        int before = stock.getAvailable() == null ? 0 : stock.getAvailable();
        int delta = target - before;
        stock.setAvailable(target);
        stock.setVersion((stock.getVersion() == null ? 0 : stock.getVersion()) + 1);
        stock.setUpdatedAt(LocalDateTime.now());
        skuStockMapper.updateById(stock);
        writeFlow(skuId, "ADMIN_SET", delta, 0, target, stock.getFrozen() == null ? 0 : stock.getFrozen(),
                operatorType, operatorId);
        log.info("stock adjusted skuId={} available={}", skuId, target);
        Map<String, Object> result = new HashMap<>();
        result.put("available", target);
        result.put("version", stock.getVersion());
        return result;
    }

    private void writeFlow(Long skuId, String changeType, int deltaAvailable, int deltaFrozen,
                           int availableAfter, int frozenAfter, String operatorType, String operatorId) {
        writeFlow(skuId, changeType, deltaAvailable, deltaFrozen, availableAfter, frozenAfter,
                operatorType, operatorId, null, null);
    }

    private void writeFlow(Long skuId, String changeType, int deltaAvailable, int deltaFrozen,
                           int availableAfter, int frozenAfter, String operatorType, String operatorId,
                           String orderNo, String remark) {
        StockFlow flow = new StockFlow();
        flow.setSkuId(skuId);
        flow.setOrderNo(orderNo);
        flow.setChangeType(changeType);
        flow.setDeltaAvailable(deltaAvailable);
        flow.setDeltaFrozen(deltaFrozen);
        flow.setAvailableAfter(availableAfter);
        flow.setFrozenAfter(frozenAfter);
        flow.setOperatorType(operatorType);
        flow.setOperatorId(operatorId);
        flow.setRemark(remark);
        flow.setCreatedAt(LocalDateTime.now());
        stockFlowMapper.insert(flow);
    }

    // ===================== Phase 3：订单库存联动 =====================

    /** 锁定库存（下单）。同一 orderNo 幂等；多 SKU 原子（事务全成全败）。 */
    @Transactional
    public Map<String, Object> lock(String orderNo, List<StockLockRequest.Item> items, Integer ttlSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusSeconds(ttlSeconds == null || ttlSeconds <= 0 ? 1800 : ttlSeconds);
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("expireAt", expireAt);

        Long existing = stockLockMapper.selectCount(
                new QueryWrapper<StockLock>().eq("order_no", orderNo));
        if (existing != null && existing > 0) {
            result.put("locked", true);
            result.put("idempotent", true);
            return result;
        }

        List<StockLockRequest.Item> sorted = items.stream()
                .sorted(Comparator.comparing(StockLockRequest.Item::getSkuId))
                .toList();
        for (StockLockRequest.Item it : sorted) {
            int affected = skuStockMapper.lock(it.getSkuId(), it.getQuantity());
            if (affected == 0) {
                throw BizException.of(ErrorCode.STOCK_LOCK_INSUFFICIENT,
                        "库存不足: skuId=" + it.getSkuId());
            }
            SkuStock stock = skuStockMapper.selectById(it.getSkuId());
            StockLock lock = new StockLock();
            lock.setOrderNo(orderNo);
            lock.setSkuId(it.getSkuId());
            lock.setQuantity(it.getQuantity());
            lock.setStatus(StockLock.LOCKED);
            lock.setExpireAt(expireAt);
            lock.setCreatedAt(now);
            lock.setUpdatedAt(now);
            stockLockMapper.insert(lock);
            writeFlow(it.getSkuId(), "ORDER_LOCK", -it.getQuantity(), it.getQuantity(),
                    stock.getAvailable(), stock.getFrozen(), "SYSTEM", null, orderNo, "lock");
        }
        log.info("stock locked orderNo={} items={}", orderNo, sorted.size());
        result.put("locked", true);
        result.put("idempotent", false);
        return result;
    }

    /** 释放库存（取消/超时/关单）。幂等：无 LOCKED 记录直接成功。 */
    @Transactional
    public Map<String, Object> release(String orderNo, String reason) {
        List<StockLock> locks = stockLockMapper.selectList(
                new QueryWrapper<StockLock>().eq("order_no", orderNo).eq("status", StockLock.LOCKED));
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        if (locks.isEmpty()) {
            result.put("released", true);
            result.put("idempotent", true);
            return result;
        }
        LocalDateTime now = LocalDateTime.now();
        for (StockLock lock : locks) {
            int affected = skuStockMapper.release(lock.getSkuId(), lock.getQuantity());
            if (affected > 0) {
                lock.setStatus(StockLock.RELEASED);
                lock.setUpdatedAt(now);
                stockLockMapper.updateById(lock);
                SkuStock stock = skuStockMapper.selectById(lock.getSkuId());
                writeFlow(lock.getSkuId(), "ORDER_RELEASE", lock.getQuantity(), -lock.getQuantity(),
                        stock.getAvailable(), stock.getFrozen(), "SYSTEM", null, orderNo, reason);
            } else {
                log.warn("release affected 0 rows orderNo={} skuId={}, lock row unchanged", orderNo, lock.getSkuId());
            }
        }
        log.info("stock released orderNo={} reason={}", orderNo, reason);
        result.put("released", true);
        result.put("idempotent", false);
        return result;
    }

    /** 扣减库存（支付成功）。frozen 归零，available 不变。幂等。 */
    @Transactional
    public Map<String, Object> deduct(String orderNo) {
        List<StockLock> locks = stockLockMapper.selectList(
                new QueryWrapper<StockLock>().eq("order_no", orderNo).eq("status", StockLock.LOCKED));
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        if (locks.isEmpty()) {
            result.put("deducted", true);
            result.put("idempotent", true);
            return result;
        }
        LocalDateTime now = LocalDateTime.now();
        for (StockLock lock : locks) {
            int affected = skuStockMapper.deduct(lock.getSkuId(), lock.getQuantity());
            if (affected > 0) {
                lock.setStatus(StockLock.DEDUCTED);
                lock.setUpdatedAt(now);
                stockLockMapper.updateById(lock);
                SkuStock stock = skuStockMapper.selectById(lock.getSkuId());
                writeFlow(lock.getSkuId(), "ORDER_DEDUCT", 0, -lock.getQuantity(),
                        stock.getAvailable(), stock.getFrozen(), "SYSTEM", null, orderNo, "deduct");
            } else {
                log.warn("deduct affected 0 rows orderNo={} skuId={}, lock row unchanged", orderNo, lock.getSkuId());
            }
        }
        log.info("stock deducted orderNo={}", orderNo);
        result.put("deducted", true);
        result.put("idempotent", false);
        return result;
    }

    /** 将 SPU 下所有在售 SKU 的可售库存同步为码池余量（虚拟/服务商品）。 */
    @Transactional
    public Map<String, Object> syncSpuPoolStock(Long spuId, int target) {
        List<com.comonon.mall.product.entity.Sku> skus = skuMapper.selectList(
                new QueryWrapper<com.comonon.mall.product.entity.Sku>()
                        .eq("spu_id", spuId)
                        .eq("status", 1));
        int safeTarget = Math.max(target, 0);
        for (com.comonon.mall.product.entity.Sku sku : skus) {
            setAvailable(sku.getId(), safeTarget, "SYSTEM", "POOL_SYNC");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("spuId", spuId);
        result.put("available", safeTarget);
        result.put("skuCount", skus.size());
        return result;
    }

    /** 退款回补：将已扣减订单的库存加回 available（实物商品）。 */
    @Transactional
    public Map<String, Object> refundRestore(String orderNo) {
        List<StockLock> locks = stockLockMapper.selectList(
                new QueryWrapper<StockLock>().eq("order_no", orderNo).eq("status", StockLock.DEDUCTED));
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        if (locks.isEmpty()) {
            result.put("restored", true);
            result.put("idempotent", true);
            return result;
        }
        LocalDateTime now = LocalDateTime.now();
        for (StockLock lock : locks) {
            int affected = skuStockMapper.restore(lock.getSkuId(), lock.getQuantity());
            lock.setStatus(StockLock.RELEASED);
            lock.setUpdatedAt(now);
            stockLockMapper.updateById(lock);
            if (affected > 0) {
                SkuStock stock = skuStockMapper.selectById(lock.getSkuId());
                writeFlow(lock.getSkuId(), "ORDER_REFUND", lock.getQuantity(), 0,
                        stock.getAvailable(), stock.getFrozen() == null ? 0 : stock.getFrozen(),
                        "SYSTEM", null, orderNo, "REFUND");
            }
        }
        log.info("stock refund restored orderNo={}", orderNo);
        result.put("restored", true);
        result.put("idempotent", false);
        return result;
    }

    /** 支付成功后延长锁过期时间，避免与待支付超时 TTL 相同导致误释放。 */
    @Transactional
    public Map<String, Object> extendLockForPaid(String orderNo) {
        LocalDateTime expireAt = LocalDateTime.now().plusDays(7);
        int n = stockLockMapper.extendExpireAt(orderNo, expireAt);
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("extended", n > 0);
        result.put("expireAt", expireAt);
        return result;
    }
}
