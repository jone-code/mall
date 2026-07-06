package com.comonon.mall.product.web;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.web.Result;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.dto.BatchProductStatusRequest;
import com.comonon.mall.product.dto.CreateSpuRequest;
import com.comonon.mall.product.dto.SaveSkusRequest;
import com.comonon.mall.product.dto.UpdateSpuRequest;
import com.comonon.mall.product.dto.UpdateStockRequest;
import com.comonon.mall.product.entity.Sku;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SkuStockMapper;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.service.PublishService;
import com.comonon.mall.product.service.SkuService;
import com.comonon.mall.product.service.SpuService;
import com.comonon.mall.product.service.StockService;
import com.comonon.mall.product.vo.AdminSpuVO;
import com.comonon.mall.product.vo.CreateSpuResultVO;
import com.comonon.mall.product.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final SpuService spuService;
    private final SkuService skuService;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final SkuStockMapper skuStockMapper;
    private final PublishService publishService;
    private final StockService stockService;

    @GetMapping("/products/stats")
    public Result<Map<String, Object>> productStats() {
        long total = spuMapper.selectCount(null);
        long onSale = spuMapper.selectCount(
                Wrappers.<Spu>lambdaQuery().eq(Spu::getStatus, SpuStatus.ON_SALE));
        long offSale = spuMapper.selectCount(
                Wrappers.<Spu>lambdaQuery().eq(Spu::getStatus, SpuStatus.OFF_SALE));
        long draft = spuMapper.selectCount(
                Wrappers.<Spu>lambdaQuery().eq(Spu::getStatus, SpuStatus.DRAFT));
        long lowStock = skuStockMapper.countLowStock(10);
        return Result.ok(Map.of(
                "total", total,
                "onSale", onSale,
                "offSale", offSale,
                "draft", draft,
                "lowStock", lowStock));
    }

    @GetMapping("/products")
    public Result<PageResult<AdminSpuVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "productType", required = false) String productType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(spuService.adminList(keyword, productType, status, categoryId, page, size));
    }

    @GetMapping("/products/{id}")
    public Result<AdminSpuVO> detail(@PathVariable Long id) {
        return Result.ok(spuService.adminDetail(id));
    }

    @PostMapping("/products")
    public Result<CreateSpuResultVO> create(@Valid @RequestBody CreateSpuRequest req) {
        return Result.ok(spuService.create(req));
    }

    @PutMapping("/products/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateSpuRequest req) {
        spuService.update(id, req);
        return Result.ok();
    }

    @PutMapping("/products/{id}/skus")
    public Result<Void> saveSkus(@PathVariable Long id, @Valid @RequestBody SaveSkusRequest req) {
        Spu spu = spuService.requireSpu(id);
        skuService.saveSkus(id, req, spu);
        return Result.ok();
    }

    @DeleteMapping("/skus/{skuId}")
    public Result<Void> deleteSku(@PathVariable Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            return Result.ok();
        }
        Spu spu = spuService.requireSpu(sku.getSpuId());
        skuService.deleteSku(skuId, spu);
        return Result.ok();
    }

    @PostMapping("/products/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, HttpServletRequest request) {
        publishService.publish(id, request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @PostMapping("/products/{id}/offline")
    public Result<Void> offline(@PathVariable Long id, HttpServletRequest request) {
        publishService.offline(id, request.getHeader("X-Admin-User-Id"));
        return Result.ok();
    }

    @PostMapping("/products/batch-status")
    public Result<Map<String, Integer>> batchStatus(@Valid @RequestBody BatchProductStatusRequest req,
                                                     HttpServletRequest request) {
        String adminId = request.getHeader("X-Admin-User-Id");
        int count = 0;
        for (Long id : req.getIds()) {
            if ("publish".equalsIgnoreCase(req.getAction())) {
                publishService.publish(id, adminId);
                count++;
            } else if ("offline".equalsIgnoreCase(req.getAction())) {
                publishService.offline(id, adminId);
                count++;
            }
        }
        return Result.ok(Map.of("count", count));
    }

    @PutMapping("/skus/{skuId}/stock")
    public Result<Map<String, Object>> updateStock(@PathVariable Long skuId,
                                                   @Valid @RequestBody UpdateStockRequest req,
                                                   HttpServletRequest request) {
        return Result.ok(stockService.setAvailable(skuId, req.getAvailable(), "ADMIN",
                request.getHeader("X-Admin-User-Id")));
    }
}
