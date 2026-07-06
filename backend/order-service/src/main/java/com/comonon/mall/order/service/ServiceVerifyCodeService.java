package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.client.ProductClient;
import com.comonon.mall.order.dto.ImportServiceVerifyCodesRequest;
import com.comonon.mall.order.dto.UpdateServiceVerifyCodeRequest;
import com.comonon.mall.order.entity.ServiceVerifyCodeEntity;
import com.comonon.mall.order.mapper.ServiceVerifyCodeMapper;
import com.comonon.mall.order.vo.FulfillmentVO;
import com.comonon.mall.order.vo.ServiceVerifyCodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ServiceVerifyCodeService {

    private final ServiceVerifyCodeMapper mapper;
    private final ProductClient productClient;

    public long countAvailable() {
        return mapper.countAvailable();
    }

    public long countEmptyPools() {
        return mapper.countEmptyPools();
    }

    public com.comonon.mall.common.web.PageResult<ServiceVerifyCodeVO> list(String status, Long spuId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        var q = Wrappers.<ServiceVerifyCodeEntity>lambdaQuery()
                .orderByDesc(ServiceVerifyCodeEntity::getId);
        if (StringUtils.hasText(status)) {
            q.eq(ServiceVerifyCodeEntity::getStatus, status);
        }
        if (spuId != null) {
            q.eq(ServiceVerifyCodeEntity::getSpuId, spuId);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ServiceVerifyCodeEntity> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(p, s);
        mapper.selectPage(mpPage, q);
        List<ServiceVerifyCodeVO> list = mpPage.getRecords().stream().map(ServiceVerifyCodeVO::from).toList();
        return com.comonon.mall.common.web.PageResult.of(list, p, s, mpPage.getTotal());
    }

    public long countAvailableBySpu(Long spuId) {
        return mapper.selectCount(Wrappers.<ServiceVerifyCodeEntity>lambdaQuery()
                .eq(ServiceVerifyCodeEntity::getStatus, ServiceVerifyCodeEntity.AVAILABLE)
                .eq(ServiceVerifyCodeEntity::getSpuId, spuId));
    }

    @Transactional
    public int importCodes(ImportServiceVerifyCodesRequest req) {
        assertServiceSpu(req.getSpuId());
        int n = 0;
        LocalDateTime now = LocalDateTime.now();
        for (ImportServiceVerifyCodesRequest.CodeItem item : req.getCodes()) {
            String code = item.getVerifyCode();
            if (code == null || code.isBlank()) {
                code = randomCode();
            } else {
                code = code.trim().toUpperCase();
            }
            ServiceVerifyCodeEntity e = new ServiceVerifyCodeEntity();
            e.setSpuId(req.getSpuId());
            e.setVerifyCode(code);
            e.setStatus(ServiceVerifyCodeEntity.AVAILABLE);
            e.setCreatedAt(now);
            try {
                mapper.insert(e);
                n++;
            } catch (Exception ex) {
                // duplicate verify_code skip
            }
        }
        syncPoolStock(req.getSpuId());
        return n;
    }

    @Transactional
    public void update(Long id, UpdateServiceVerifyCodeRequest req) {
        ServiceVerifyCodeEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw BizException.of(ErrorCode.SERVICE_VERIFY_NOT_FOUND, "核销码不存在");
        }
        if (!ServiceVerifyCodeEntity.AVAILABLE.equals(entity.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "仅可用核销码可编辑");
        }
        assertServiceSpu(req.getSpuId());
        entity.setSpuId(req.getSpuId());
        entity.setVerifyCode(req.getVerifyCode().trim().toUpperCase());
        try {
            mapper.updateById(entity);
        } catch (Exception ex) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "核销码已存在");
        }
    }

    @Transactional
    public void reserveForOrder(String orderNo, Long spuId) {
        if (spuId == null) {
            throw BizException.of(ErrorCode.SERVICE_VERIFY_NOT_AVAILABLE, "服务商品核销码库存不足");
        }
        ServiceVerifyCodeEntity existing = mapper.findReservedByOrderNo(orderNo);
        if (existing != null) {
            return;
        }
        ServiceVerifyCodeEntity code = mapper.lockOneAvailable(spuId);
        if (code == null) {
            throw BizException.of(ErrorCode.SERVICE_VERIFY_NOT_AVAILABLE,
                    "服务商品核销码库存不足，请先在后台录入并绑定该商品的核销码");
        }
        int n = mapper.markReserved(code.getId(), orderNo);
        if (n == 0) {
            throw BizException.of(ErrorCode.SERVICE_VERIFY_NOT_AVAILABLE, "服务商品核销码库存不足");
        }
        syncPoolStock(spuId);
    }

    @Transactional
    public void releaseReservation(String orderNo) {
        List<ServiceVerifyCodeEntity> reserved = mapper.selectList(
                Wrappers.<ServiceVerifyCodeEntity>lambdaQuery()
                        .eq(ServiceVerifyCodeEntity::getOrderNo, orderNo)
                        .eq(ServiceVerifyCodeEntity::getStatus, ServiceVerifyCodeEntity.RESERVED));
        if (reserved.isEmpty()) {
            return;
        }
        mapper.releaseReservation(orderNo);
        reserved.stream().map(ServiceVerifyCodeEntity::getSpuId).distinct().forEach(this::syncPoolStock);
    }

    @Transactional
    public FulfillmentVO issueReserved(String orderNo, Long spuId) {
        if (spuId == null) {
            return null;
        }
        ServiceVerifyCodeEntity reserved = mapper.findReservedByOrderNo(orderNo);
        if (reserved != null) {
            LocalDateTime now = LocalDateTime.now();
            int n = mapper.markIssuedFromReserved(reserved.getId(), orderNo, now);
            if (n > 0) {
                FulfillmentVO vo = new FulfillmentVO();
                vo.setVerifyCode(reserved.getVerifyCode());
                syncPoolStock(spuId);
                return vo;
            }
        }
        return allocate(orderNo, spuId);
    }

    @Transactional
    public FulfillmentVO allocate(String orderNo, Long spuId) {
        if (spuId == null) {
            return null;
        }
        ServiceVerifyCodeEntity code = mapper.lockOneAvailable(spuId);
        if (code == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        int n = mapper.markIssued(code.getId(), orderNo, now);
        if (n == 0) {
            return null;
        }
        FulfillmentVO vo = new FulfillmentVO();
        vo.setVerifyCode(code.getVerifyCode());
        syncPoolStock(spuId);
        return vo;
    }

    @Transactional
    public void revokeAfterRefund(String orderNo) {
        mapper.releaseReservation(orderNo);
        List<ServiceVerifyCodeEntity> issued = mapper.selectList(
                Wrappers.<ServiceVerifyCodeEntity>lambdaQuery()
                        .eq(ServiceVerifyCodeEntity::getOrderNo, orderNo)
                        .eq(ServiceVerifyCodeEntity::getStatus, ServiceVerifyCodeEntity.ISSUED));
        if (issued.isEmpty()) {
            return;
        }
        mapper.markRevokedByOrderNo(orderNo);
        issued.stream().map(ServiceVerifyCodeEntity::getSpuId).distinct().forEach(this::syncPoolStock);
    }

    private void syncPoolStock(Long spuId) {
        if (spuId == null) {
            return;
        }
        int available = (int) Math.min(countAvailableBySpu(spuId), Integer.MAX_VALUE);
        productClient.syncSpuPoolStock(spuId, available);
    }

    private void assertServiceSpu(Long spuId) {
        if (spuId == null) {
            throw BizException.of(ErrorCode.SERVICE_VERIFY_SPU_INVALID, "请选择服务商品");
        }
        productClient.assertServiceSpu(spuId);
    }

    private static String randomCode() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "SVC" + String.format("%08d", r.nextInt(0, 100_000_000));
    }
}
