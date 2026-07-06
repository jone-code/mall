package com.comonon.mall.product.service;

import com.comonon.mall.product.client.SearchIndexClient;
import com.comonon.mall.product.domain.SpuPublishValidator;
import com.comonon.mall.product.domain.SpuStatus;
import com.comonon.mall.product.entity.Spu;
import com.comonon.mall.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishService {

    private final SpuMapper spuMapper;
    private final SpuPublishValidator publishValidator;
    private final SpuService spuService;
    private final SearchIndexClient searchIndexClient;

    @Transactional
    public void publish(Long spuId, String adminUserId) {
        Spu spu = spuService.requireSpu(spuId);
        publishValidator.validate(spu);
        spu.setStatus(SpuStatus.ON_SALE);
        spu.setUpdatedAt(LocalDateTime.now());
        spuMapper.updateById(spu);
        log.info("SPU published spuId={} adminUserId={}", spuId, adminUserId);
        searchIndexClient.indexSpu(spuId);
    }

    @Transactional
    public void offline(Long spuId, String adminUserId) {
        Spu spu = spuService.requireSpu(spuId);
        spu.setStatus(SpuStatus.OFF_SALE);
        spu.setUpdatedAt(LocalDateTime.now());
        spuMapper.updateById(spu);
        log.info("SPU offline spuId={} adminUserId={}", spuId, adminUserId);
        searchIndexClient.deleteSpu(spuId);
    }
}
