package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.domain.SpecJson;
import com.comonon.mall.product.domain.SpecTextGenerator;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.dto.CreateSpuRequest;
import com.comonon.mall.product.dto.SaveSkusRequest;
import com.comonon.mall.product.entity.Sku;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.domain.SpuPublishValidator;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.AdminSkuVO;
import com.comonon.mall.product.vo.AdminSpuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final StockService stockService;
    private final JsonHelper jsonHelper;
    private final SpuPublishValidator publishValidator;

    @Transactional
    public Long createDefaultSku(Long spuId, CreateSpuRequest.DefaultSkuPayload payload) {
        return insertSku(spuId, null, payload.getSpecJson(), payload.getPrice(),
                payload.getMarketPrice(), 1, 1, payload.getAvailable());
    }

    @Transactional
    public void saveSkus(Long spuId, SaveSkusRequest req, Spu spu) {
        Set<String> specTexts = new HashSet<>();
        for (SaveSkusRequest.SkuItem item : req.getSkus()) {
            String specText = SpecTextGenerator.generate(item.getSpecJson());
            if (!specTexts.add(specText)) {
                throw BizException.of(ErrorCode.SKU_SPEC_DUPLICATE, "规格重复: " + specText);
            }
            if (item.getId() != null) {
                updateSku(spuId, item, specText);
            } else {
                insertSku(spuId, item.getSkuCode(), item.getSpecJson(), item.getPrice(),
                        item.getMarketPrice(),
                        item.getIsDefault() == null ? 0 : item.getIsDefault(),
                        item.getStatus() == null ? 1 : item.getStatus(),
                        item.getAvailable());
            }
        }
        ensureSingleDefault(spuId);
        if (spu.getStatus() != null && spu.getStatus() == SpuStatus.ON_SALE) {
            publishValidator.validate(spu);
        }
        maybeAutoOffline(spu);
    }

    private void updateSku(Long spuId, SaveSkusRequest.SkuItem item, String specText) {
        Sku sku = skuMapper.selectById(item.getId());
        if (sku == null || !spuId.equals(sku.getSpuId())) {
            throw BizException.of(ErrorCode.SKU_NOT_FOUND, "SKU 不存在");
        }
        checkDuplicateSpec(spuId, specText, sku.getId());
        sku.setSkuCode(item.getSkuCode());
        sku.setSpecJson(jsonHelper.toJson(item.getSpecJson()));
        sku.setSpecText(specText);
        sku.setPrice(item.getPrice());
        sku.setMarketPrice(item.getMarketPrice());
        if (item.getIsDefault() != null) {
            sku.setIsDefault(item.getIsDefault());
        }
        if (item.getStatus() != null) {
            sku.setStatus(item.getStatus());
        }
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        if (item.getAvailable() != null) {
            stockService.setAvailable(sku.getId(), item.getAvailable(), "ADMIN", null);
        }
    }

    private Long insertSku(Long spuId, String skuCode, SpecJson specJson, BigDecimal price,
                           BigDecimal marketPrice, int isDefault, int status, Integer available) {
        String specText = SpecTextGenerator.generate(specJson);
        checkDuplicateSpec(spuId, specText, null);
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        sku.setSkuCode(skuCode);
        sku.setSpecJson(jsonHelper.toJson(specJson));
        sku.setSpecText(specText);
        sku.setPrice(price);
        sku.setMarketPrice(marketPrice);
        sku.setIsDefault(isDefault);
        sku.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        sku.setCreatedAt(now);
        sku.setUpdatedAt(now);
        skuMapper.insert(sku);
        stockService.initStock(sku.getId(), available == null ? 0 : available);
        return sku.getId();
    }

    private void checkDuplicateSpec(Long spuId, String specText, Long excludeId) {
        LambdaQueryWrapper<Sku> qw = new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .eq(Sku::getSpecText, specText);
        if (excludeId != null) {
            qw.ne(Sku::getId, excludeId);
        }
        if (skuMapper.selectCount(qw) > 0) {
            throw BizException.of(ErrorCode.SKU_SPEC_DUPLICATE, "规格重复: " + specText);
        }
    }

    private void ensureSingleDefault(Long spuId) {
        List<Sku> enabled = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .eq(Sku::getStatus, 1));
        long defaults = enabled.stream().filter(s -> s.getIsDefault() != null && s.getIsDefault() == 1).count();
        if (defaults == 0 && !enabled.isEmpty()) {
            Sku first = enabled.get(0);
            first.setIsDefault(1);
            first.setUpdatedAt(LocalDateTime.now());
            skuMapper.updateById(first);
        } else if (defaults > 1) {
            boolean kept = false;
            for (Sku s : enabled) {
                if (s.getIsDefault() != null && s.getIsDefault() == 1) {
                    if (!kept) {
                        kept = true;
                    } else {
                        s.setIsDefault(0);
                        s.setUpdatedAt(LocalDateTime.now());
                        skuMapper.updateById(s);
                    }
                }
            }
        }
    }

    @Transactional
    public void deleteSku(Long skuId, Spu spu) {
        if (spu.getStatus() != null && spu.getStatus() == SpuStatus.ON_SALE) {
            throw BizException.of(ErrorCode.SKU_DELETE_FORBIDDEN, "上架中商品不可删除 SKU");
        }
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw BizException.of(ErrorCode.SKU_NOT_FOUND, "SKU 不存在");
        }
        long count = skuMapper.selectCount(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spu.getId()));
        if (count <= 1) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "至少保留一个 SKU");
        }
        skuMapper.deleteById(skuId);
        stockService.deleteStock(skuId);
    }

    void maybeAutoOffline(Spu spu) {
        if (spu.getStatus() == null || spu.getStatus() != SpuStatus.ON_SALE) {
            return;
        }
        long enabled = skuMapper.selectCount(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spu.getId())
                .eq(Sku::getStatus, 1));
        if (enabled == 0) {
            spu.setStatus(SpuStatus.OFF_SALE);
            spu.setUpdatedAt(LocalDateTime.now());
            spuMapper.updateById(spu);
        }
    }

    public AdminSpuVO toAdminSpuSummary(Spu spu) {
        AdminSpuVO vo = baseAdminSpu(spu);
        vo.setMinPrice(skuMapper.minPrice(spu.getId()));
        vo.setMaxPrice(skuMapper.maxPrice(spu.getId()));
        return vo;
    }

    public AdminSpuVO toAdminSpuDetail(Spu spu) {
        AdminSpuVO vo = toAdminSpuSummary(spu);
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spu.getId()));
        for (Sku sku : skus) {
            AdminSkuVO svo = new AdminSkuVO();
            svo.setId(sku.getId());
            svo.setSpuId(sku.getSpuId());
            svo.setSkuCode(sku.getSkuCode());
            svo.setSpecJson(jsonHelper.parseSpec(sku.getSpecJson()));
            svo.setSpecText(sku.getSpecText());
            svo.setPrice(sku.getPrice());
            svo.setMarketPrice(sku.getMarketPrice());
            svo.setIsDefault(sku.getIsDefault());
            svo.setStatus(sku.getStatus());
            svo.setAvailable(stockService.getAvailable(sku.getId()));
            vo.getSkus().add(svo);
        }
        return vo;
    }

    private AdminSpuVO baseAdminSpu(Spu spu) {
        AdminSpuVO vo = new AdminSpuVO();
        vo.setId(spu.getId());
        vo.setCategoryId(spu.getCategoryId());
        vo.setTitle(spu.getTitle());
        vo.setSubtitle(spu.getSubtitle());
        vo.setProductType(spu.getProductType());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(jsonHelper.parseImages(spu.getImages()));
        vo.setDetailHtml(spu.getDetailHtml());
        vo.setStatus(spu.getStatus());
        vo.setSortOrder(spu.getSortOrder());
        return vo;
    }

    public List<Sku> listEnabledBySpu(Long spuId) {
        return skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .eq(Sku::getStatus, 1)
                .orderByDesc(Sku::getIsDefault)
                .orderByAsc(Sku::getId));
    }
}
