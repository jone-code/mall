package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.product.domain.SpuPublishValidator;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.entity.Category;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.HomeVO;
import com.comonon.mall.product.vo.PageResult;
import com.comonon.mall.product.vo.SkuDetailVO;
import com.comonon.mall.product.vo.SpuDetailVO;
import com.comonon.mall.product.vo.SpuIndexVO;
import com.comonon.mall.product.vo.SpuSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpuQueryService {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final CategoryService categoryService;
    private final SkuService skuService;
    private final StockService stockService;
    private final JsonHelper jsonHelper;

    public HomeVO home() {
        HomeVO vo = new HomeVO();
        vo.setCategories(categoryService.treeForConsumer());
        List<Spu> top = spuMapper.selectList(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, SpuStatus.ON_SALE)
                .orderByDesc(Spu::getSortOrder)
                .orderByDesc(Spu::getId)
                .last("LIMIT 20"));
        for (Spu spu : top) {
            SpuSummaryVO s = toSummary(spu);
            if (s.getMinPrice() != null) {
                vo.getRecommendSpus().add(s);
            }
        }
        return vo;
    }

    public PageResult<SpuSummaryVO> listProducts(Long categoryId, String keyword, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);
        List<Long> categoryIds = categoryService.resolveCategoryIdsForQuery(categoryId);
        if (categoryIds.size() == 1 && categoryIds.get(0) == -1L) {
            return PageResult.of(List.of(), p, s, 0);
        }
        LambdaQueryWrapper<Spu> qw = new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, SpuStatus.ON_SALE)
                .orderByDesc(Spu::getSortOrder)
                .orderByDesc(Spu::getId);
        if (!categoryIds.isEmpty()) {
            qw.in(Spu::getCategoryId, categoryIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Spu::getTitle, escapeLike(keyword));
        }
        long total = spuMapper.selectCount(qw);
        qw.last("LIMIT " + ((p - 1) * s) + "," + s);
        List<Spu> rows = spuMapper.selectList(qw);
        List<SpuSummaryVO> pageList = new ArrayList<>();
        for (Spu spu : rows) {
            SpuSummaryVO summary = toSummary(spu);
            if (summary.getMinPrice() != null) {
                pageList.add(summary);
            }
        }
        return PageResult.of(pageList, p, s, total);
    }

    /** 转义 LIKE 通配符，避免用户输入 %/_ 匹配全部。 */
    private static String escapeLike(String keyword) {
        return keyword.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public SpuDetailVO detail(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null || spu.getStatus() == null || spu.getStatus() != SpuStatus.ON_SALE) {
            throw BizException.of(ErrorCode.SPU_NOT_FOUND, "商品不存在");
        }
        Category cat = categoryService.requireCategory(spu.getCategoryId());
        if (cat.getStatus() == null || cat.getStatus() != 1) {
            throw BizException.of(ErrorCode.SPU_NOT_FOUND, "商品不存在");
        }
        SpuDetailVO vo = new SpuDetailVO();
        vo.setId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setSubtitle(spu.getSubtitle());
        vo.setProductType(spu.getProductType());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(jsonHelper.parseImages(spu.getImages()));
        vo.setDetailHtml(spu.getDetailHtml());
        for (var sku : skuService.listEnabledBySpu(spuId)) {
            SkuDetailVO svo = new SkuDetailVO();
            svo.setId(sku.getId());
            svo.setSpecText(sku.getSpecText());
            svo.setSpecJson(jsonHelper.parseSpec(sku.getSpecJson()));
            svo.setPrice(sku.getPrice());
            svo.setMarketPrice(sku.getMarketPrice());
            svo.setIsDefault(sku.getIsDefault());
            svo.setAvailable(stockService.getAvailable(sku.getId()));
            vo.getSkus().add(svo);
        }
        return vo;
    }

    public PageResult<SpuIndexVO> listOnSaleForIndex(int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        LambdaQueryWrapper<Spu> qw = new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, SpuStatus.ON_SALE)
                .orderByDesc(Spu::getSortOrder)
                .orderByDesc(Spu::getId);
        long total = spuMapper.selectCount(qw);
        qw.last("LIMIT " + ((p - 1) * s) + "," + s);
        List<SpuIndexVO> list = new ArrayList<>();
        for (Spu spu : spuMapper.selectList(qw)) {
            SpuIndexVO doc = toIndex(spu);
            if (doc.getMinPrice() != null) {
                list.add(doc);
            }
        }
        return PageResult.of(list, p, s, total);
    }

    public SpuIndexVO indexDoc(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null || spu.getStatus() == null || spu.getStatus() != SpuStatus.ON_SALE) {
            return null;
        }
        SpuIndexVO doc = toIndex(spu);
        return doc.getMinPrice() == null ? null : doc;
    }

    private SpuIndexVO toIndex(Spu spu) {
        SpuIndexVO vo = new SpuIndexVO();
        vo.setId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setSubtitle(spu.getSubtitle());
        vo.setCategoryId(spu.getCategoryId());
        vo.setProductType(spu.getProductType());
        vo.setMainImage(spu.getMainImage());
        vo.setMinPrice(skuMapper.minPrice(spu.getId()));
        vo.setMaxPrice(skuMapper.maxPrice(spu.getId()));
        vo.setSortOrder(spu.getSortOrder());
        return vo;
    }

    private SpuSummaryVO toSummary(Spu spu) {
        SpuSummaryVO vo = new SpuSummaryVO();
        vo.setId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setSubtitle(spu.getSubtitle());
        vo.setMainImage(spu.getMainImage());
        vo.setProductType(spu.getProductType());
        vo.setMinPrice(skuMapper.minPrice(spu.getId()));
        vo.setMaxPrice(skuMapper.maxPrice(spu.getId()));
        return vo;
    }
}
