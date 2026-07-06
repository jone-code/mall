package com.comonon.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.entity.Sku;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SkuMapper;
import com.comonon.mall.product.mapper.SpuMapper;
import com.comonon.mall.product.vo.SkuSnapshotListVO;
import com.comonon.mall.product.vo.SkuSnapshotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuSnapshotService {

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final StockService stockService;

    public SkuSnapshotListVO snapshot(List<Long> skuIds) {
        SkuSnapshotListVO result = new SkuSnapshotListVO();
        if (skuIds == null || skuIds.isEmpty()) {
            return result;
        }
        List<SkuSnapshotVO> items = new ArrayList<>();
        for (Long skuId : skuIds) {
            Sku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                continue;
            }
            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null) {
                continue;
            }
            int available = stockService.getAvailable(skuId);
            SkuSnapshotVO vo = new SkuSnapshotVO();
            vo.setSkuId(skuId);
            vo.setSpuId(spu.getId());
            vo.setSpecText(sku.getSpecText());
            vo.setPrice(sku.getPrice());
            vo.setMarketPrice(sku.getMarketPrice());
            vo.setAvailable(available);
            vo.setSkuStatus(sku.getStatus());
            vo.setSpuStatus(spu.getStatus());
            vo.setSpuTitle(spu.getTitle());
            vo.setMainImage(spu.getMainImage());
            vo.setProductType(spu.getProductType());
            String reason = null;
            boolean sellable = true;
            if (spu.getStatus() == null || spu.getStatus() != SpuStatus.ON_SALE) {
                sellable = false;
                reason = "OFF_SALE";
            } else if (sku.getStatus() == null || sku.getStatus() != 1) {
                sellable = false;
                reason = "SKU_DISABLED";
            } else if (available <= 0) {
                sellable = false;
                reason = "OUT_OF_STOCK";
            }
            vo.setSellable(sellable);
            vo.setInvalidReason(reason);
            items.add(vo);
        }
        result.setItems(items);
        return result;
    }

    public SkuSnapshotVO getOne(Long skuId) {
        SkuSnapshotListVO list = snapshot(List.of(skuId));
        if (list.getItems().isEmpty()) {
            return null;
        }
        return list.getItems().get(0);
    }
}
