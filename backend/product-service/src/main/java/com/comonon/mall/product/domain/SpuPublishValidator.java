package com.comonon.mall.product.domain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.entity.Category;
import com.comonon.mall.product.entity.Sku;
import com.comonon.mall.product.entity.SkuStock;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.CategoryMapper;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpuPublishValidator {

    private final CategoryMapper categoryMapper;
    private final SkuMapper skuMapper;
    private final SkuStockMapper skuStockMapper;

    public void validate(Spu spu) {
        if (spu == null) {
            throw BizException.of(ErrorCode.SPU_NOT_FOUND, "商品不存在");
        }
        Category cat = categoryMapper.selectById(spu.getCategoryId());
        if (cat == null || cat.getStatus() == null || cat.getStatus() != 1) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "类目不存在或已禁用");
        }
        if (cat.getLevel() == null || cat.getLevel() != 2) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "商品须挂在二级类目");
        }
        if (!StringUtils.hasText(spu.getTitle()) || !StringUtils.hasText(spu.getMainImage())) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "标题与主图不能为空");
        }
        List<Sku> enabled = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spu.getId())
                .eq(Sku::getStatus, 1));
        if (enabled.isEmpty()) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "至少需要一个启用的 SKU");
        }
        long defaultCount = enabled.stream().filter(s -> s.getIsDefault() != null && s.getIsDefault() == 1).count();
        if (defaultCount != 1) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "须指定唯一默认 SKU");
        }
        boolean hasStock = false;
        for (Sku sku : enabled) {
            if (sku.getPrice() == null || sku.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "启用 SKU 售价须大于 0");
            }
            SkuStock stock = skuStockMapper.selectById(sku.getId());
            if (stock != null && stock.getAvailable() != null && stock.getAvailable() > 0) {
                hasStock = true;
            }
        }
        if (!hasStock) {
            throw BizException.of(ErrorCode.SPU_NOT_PUBLISHABLE, "至少一个启用 SKU 库存大于 0");
        }
    }
}
