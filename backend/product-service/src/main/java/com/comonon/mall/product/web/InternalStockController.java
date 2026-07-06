package com.comonon.mall.product.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.product.dto.StockLockRequest;
import com.comonon.mall.product.dto.StockOpRequest;
import com.comonon.mall.product.dto.SyncSpuPoolRequest;
import com.comonon.mall.product.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部库存接口，供 order-service 调用。
 */
@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class InternalStockController {

    private final StockService stockService;

    @PostMapping("/lock")
    public Result<Map<String, Object>> lock(@Valid @RequestBody StockLockRequest req) {
        return Result.ok(stockService.lock(req.getOrderNo(), req.getItems(), req.getTtlSeconds()));
    }

    @PostMapping("/release")
    public Result<Map<String, Object>> release(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.release(req.getOrderNo(), req.getReason()));
    }

    @PostMapping("/deduct")
    public Result<Map<String, Object>> deduct(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.deduct(req.getOrderNo()));
    }

    @PostMapping("/sync-spu-pool")
    public Result<Map<String, Object>> syncSpuPool(@Valid @RequestBody SyncSpuPoolRequest req) {
        return Result.ok(stockService.syncSpuPoolStock(req.getSpuId(), req.getAvailable()));
    }

    @PostMapping("/refund-restore")
    public Result<Map<String, Object>> refundRestore(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.refundRestore(req.getOrderNo()));
    }

    @PostMapping("/extend-lock")
    public Result<Map<String, Object>> extendLock(@Valid @RequestBody StockOpRequest req) {
        return Result.ok(stockService.extendLockForPaid(req.getOrderNo()));
    }
}
