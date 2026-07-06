package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.domain.ProductType;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.dto.CreateSpuRequest;
import com.comonon.mall.product.dto.UpdateSpuRequest;
import com.comonon.mall.product.entity.Sku;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.AdminSpuVO;
import com.comonon.mall.product.vo.CreateSpuResultVO;
import com.comonon.mall.product.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpuService {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final CategoryService categoryService;
    private final SkuService skuService;
    private final JsonHelper jsonHelper;

    @Transactional
    public CreateSpuResultVO create(CreateSpuRequest req) {
        if (!ProductType.isValid(req.getProductType())) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "无效的商品类型");
        }
        categoryService.requireEnabledLevel2(req.getCategoryId());
        Spu spu = new Spu();
        spu.setCategoryId(req.getCategoryId());
        spu.setTitle(req.getTitle().trim());
        spu.setSubtitle(req.getSubtitle());
        spu.setProductType(req.getProductType());
        spu.setMainImage(req.getMainImage());
        spu.setImages(jsonHelper.toJson(req.getImages() == null ? List.of() : req.getImages()));
        spu.setDetailHtml(req.getDetailHtml());
        spu.setStatus(SpuStatus.DRAFT);
        spu.setSortOrder(0);
        LocalDateTime now = LocalDateTime.now();
        spu.setCreatedAt(now);
        spu.setUpdatedAt(now);
        spuMapper.insert(spu);

        Long skuId = skuService.createDefaultSku(spu.getId(), req.getDefaultSku());
        CreateSpuResultVO vo = new CreateSpuResultVO();
        vo.setSpuId(spu.getId());
        vo.setSkuId(skuId);
        return vo;
    }

    @Transactional
    public void update(Long id, UpdateSpuRequest req) {
        Spu spu = requireSpu(id);
        if (req.getCategoryId() != null) {
            categoryService.requireEnabledLevel2(req.getCategoryId());
            spu.setCategoryId(req.getCategoryId());
        }
        if (req.getTitle() != null) {
            spu.setTitle(req.getTitle().trim());
        }
        if (req.getSubtitle() != null) {
            spu.setSubtitle(req.getSubtitle());
        }
        if (req.getMainImage() != null) {
            spu.setMainImage(req.getMainImage());
        }
        if (req.getImages() != null) {
            spu.setImages(jsonHelper.toJson(req.getImages()));
        }
        if (req.getDetailHtml() != null) {
            spu.setDetailHtml(req.getDetailHtml());
        }
        if (req.getSortOrder() != null) {
            spu.setSortOrder(req.getSortOrder());
        }
        spu.setUpdatedAt(LocalDateTime.now());
        spuMapper.updateById(spu);
    }

    public Spu requireSpu(Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw BizException.of(ErrorCode.SPU_NOT_FOUND, "商品不存在");
        }
        return spu;
    }

    public PageResult<AdminSpuVO> adminList(String keyword, String productType, Integer status,
                                            Long categoryId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        LambdaQueryWrapper<Spu> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Spu::getTitle, keyword.trim());
        }
        if (StringUtils.hasText(productType)) {
            qw.eq(Spu::getProductType, productType);
        }
        if (status != null) {
            qw.eq(Spu::getStatus, status);
        }
        if (categoryId != null) {
            List<Long> ids = categoryService.resolveCategoryIdsForQuery(categoryId);
            if (!ids.isEmpty()) {
                qw.in(Spu::getCategoryId, ids);
            }
        }
        qw.orderByDesc(Spu::getSortOrder).orderByDesc(Spu::getId);
        long total = spuMapper.selectCount(qw);
        qw.last("LIMIT " + ((p - 1) * s) + "," + s);
        List<Spu> rows = spuMapper.selectList(qw);
        List<AdminSpuVO> list = new ArrayList<>();
        for (Spu spu : rows) {
            list.add(skuService.toAdminSpuSummary(spu));
        }
        return PageResult.of(list, p, s, total);
    }

    public AdminSpuVO adminDetail(Long id) {
        Spu spu = requireSpu(id);
        return skuService.toAdminSpuDetail(spu);
    }

    @Transactional
    public CreateSpuResultVO copy(Long sourceId) {
        Spu source = requireSpu(sourceId);
        Spu spu = new Spu();
        spu.setCategoryId(source.getCategoryId());
        spu.setTitle(source.getTitle() + " (副本)");
        spu.setSubtitle(source.getSubtitle());
        spu.setProductType(source.getProductType());
        spu.setMainImage(source.getMainImage());
        spu.setImages(source.getImages());
        spu.setDetailHtml(source.getDetailHtml());
        spu.setStatus(SpuStatus.DRAFT);
        spu.setSortOrder(source.getSortOrder() == null ? 0 : source.getSortOrder());
        LocalDateTime now = LocalDateTime.now();
        spu.setCreatedAt(now);
        spu.setUpdatedAt(now);
        spuMapper.insert(spu);

        List<Sku> sourceSkus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, sourceId)
                .orderByAsc(Sku::getId));
        if (sourceSkus.isEmpty()) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "源商品无 SKU，无法复制");
        }
        Long firstSkuId = skuService.cloneSkus(spu.getId(), sourceSkus);
        CreateSpuResultVO vo = new CreateSpuResultVO();
        vo.setSpuId(spu.getId());
        vo.setSkuId(firstSkuId);
        return vo;
    }
}
