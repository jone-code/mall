package com.comonon.mall.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.order.client.ProductClient;
import com.comonon.mall.order.dto.ImportVirtualCardsRequest;
import com.comonon.mall.order.dto.UpdateVirtualCardRequest;
import com.comonon.mall.order.entity.VirtualCardEntity;
import com.comonon.mall.order.mapper.VirtualCardMapper;
import com.comonon.mall.order.vo.FulfillmentVO;
import com.comonon.mall.order.vo.ImportCardsResultVO;
import com.comonon.mall.order.vo.VirtualCardPoolVO;
import com.comonon.mall.order.vo.VirtualCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VirtualCardService {

    private static final String VIRTUAL = "VIRTUAL";

    private final VirtualCardMapper virtualCardMapper;
    private final ProductClient productClient;

    public long countAvailable() {
        return virtualCardMapper.countAvailable();
    }

    public long countEmptyPools() {
        return virtualCardMapper.countEmptyPools();
    }

    public com.comonon.mall.common.web.PageResult<VirtualCardVO> list(String status, Long spuId, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        var q = Wrappers.<VirtualCardEntity>lambdaQuery().orderByDesc(VirtualCardEntity::getId);
        if (StringUtils.hasText(status)) {
            q.eq(VirtualCardEntity::getStatus, status);
        }
        if (spuId != null) {
            q.eq(VirtualCardEntity::getSpuId, spuId);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<VirtualCardEntity> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(p, s);
        virtualCardMapper.selectPage(mpPage, q);
        List<VirtualCardVO> list = mpPage.getRecords().stream().map(VirtualCardVO::from).toList();
        return com.comonon.mall.common.web.PageResult.of(list, p, s, mpPage.getTotal());
    }

    public long countAvailableBySpu(Long spuId) {
        return virtualCardMapper.selectCount(Wrappers.<VirtualCardEntity>lambdaQuery()
                .eq(VirtualCardEntity::getStatus, VirtualCardEntity.AVAILABLE)
                .eq(VirtualCardEntity::getSpuId, spuId));
    }

    @Transactional
    public ImportCardsResultVO importCards(ImportVirtualCardsRequest req) {
        assertVirtualSpu(req.getSpuId());
        ImportCardsResultVO result = new ImportCardsResultVO();
        LocalDateTime now = LocalDateTime.now();
        for (ImportVirtualCardsRequest.CardItem item : req.getCards()) {
            if (item.getCardNo() == null || item.getCardNo().isBlank()) {
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }
            String secret = item.getCardSecret();
            if (secret == null || secret.isBlank()) {
                secret = randomDigits(16);
            }
            VirtualCardEntity e = new VirtualCardEntity();
            e.setSpuId(req.getSpuId());
            e.setCardNo(item.getCardNo().trim());
            e.setCardSecret(secret.trim());
            e.setStatus(VirtualCardEntity.AVAILABLE);
            e.setCreatedAt(now);
            try {
                virtualCardMapper.insert(e);
                result.setImported(result.getImported() + 1);
            } catch (Exception ex) {
                result.setDuplicate(result.getDuplicate() + 1);
            }
        }
        syncPoolStock(req.getSpuId());
        return result;
    }

    public List<com.comonon.mall.order.vo.VirtualCardPoolVO> poolSummary() {
        return virtualCardMapper.poolSummary().stream().map(row -> {
            com.comonon.mall.order.vo.VirtualCardPoolVO vo = new com.comonon.mall.order.vo.VirtualCardPoolVO();
            vo.setSpuId(((Number) row.get("spuId")).longValue());
            vo.setAvailable(((Number) row.get("available")).longValue());
            vo.setIssued(((Number) row.get("issued")).longValue());
            vo.setTotal(((Number) row.get("total")).longValue());
            return vo;
        }).toList();
    }

    @Transactional
    public void update(Long id, UpdateVirtualCardRequest req) {
        VirtualCardEntity card = virtualCardMapper.selectById(id);
        if (card == null) {
            throw BizException.of(ErrorCode.VIRTUAL_CARD_NOT_FOUND, "卡密不存在");
        }
        if (!VirtualCardEntity.AVAILABLE.equals(card.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "仅可用卡密可编辑");
        }
        assertVirtualSpu(req.getSpuId());
        card.setSpuId(req.getSpuId());
        card.setCardNo(req.getCardNo().trim());
        if (StringUtils.hasText(req.getCardSecret())) {
            card.setCardSecret(req.getCardSecret().trim());
        }
        try {
            virtualCardMapper.updateById(card);
        } catch (Exception ex) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "卡号已存在");
        }
    }

    /** 下单时预占一张卡密，防止多笔待支付单争抢同一码。 */
    @Transactional
    public void reserveForOrder(String orderNo, Long spuId) {
        if (spuId == null) {
            throw BizException.of(ErrorCode.VIRTUAL_CARD_NOT_AVAILABLE, "虚拟商品卡密库存不足");
        }
        VirtualCardEntity existing = virtualCardMapper.findReservedByOrderNo(orderNo);
        if (existing != null) {
            return;
        }
        VirtualCardEntity card = virtualCardMapper.lockOneAvailable(spuId);
        if (card == null) {
            throw BizException.of(ErrorCode.VIRTUAL_CARD_NOT_AVAILABLE,
                    "虚拟商品卡密库存不足，请先在后台录入并绑定该商品的卡密");
        }
        int n = virtualCardMapper.markReserved(card.getId(), orderNo);
        if (n == 0) {
            throw BizException.of(ErrorCode.VIRTUAL_CARD_NOT_AVAILABLE, "虚拟商品卡密库存不足");
        }
        syncPoolStock(spuId);
    }

    /** 取消/超时释放预占。 */
    @Transactional
    public void releaseReservation(String orderNo) {
        List<VirtualCardEntity> reserved = virtualCardMapper.selectList(
                Wrappers.<VirtualCardEntity>lambdaQuery()
                        .eq(VirtualCardEntity::getOrderNo, orderNo)
                        .eq(VirtualCardEntity::getStatus, VirtualCardEntity.RESERVED));
        if (reserved.isEmpty()) {
            return;
        }
        virtualCardMapper.releaseReservation(orderNo);
        reserved.stream().map(VirtualCardEntity::getSpuId).distinct().forEach(this::syncPoolStock);
    }

    /** 支付后发放：优先使用本单预占卡密。 */
    @Transactional
    public FulfillmentVO issueReserved(String orderNo, Long spuId) {
        if (spuId == null) {
            return null;
        }
        VirtualCardEntity reserved = virtualCardMapper.findReservedByOrderNo(orderNo);
        if (reserved != null) {
            LocalDateTime now = LocalDateTime.now();
            int n = virtualCardMapper.markIssuedFromReserved(reserved.getId(), orderNo, now);
            if (n > 0) {
                FulfillmentVO vo = new FulfillmentVO();
                vo.setCardNo(reserved.getCardNo());
                vo.setCardSecret(reserved.getCardSecret());
                syncPoolStock(spuId);
                return vo;
            }
        }
        return allocate(orderNo, spuId);
    }

    /** 从卡池分配（兼容旧单无预占）。 */
    @Transactional
    public FulfillmentVO allocate(String orderNo, Long spuId) {
        if (spuId == null) {
            return null;
        }
        VirtualCardEntity card = virtualCardMapper.lockOneAvailable(spuId);
        if (card == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        int n = virtualCardMapper.markIssued(card.getId(), orderNo, now);
        if (n == 0) {
            return null;
        }
        FulfillmentVO vo = new FulfillmentVO();
        vo.setCardNo(card.getCardNo());
        vo.setCardSecret(card.getCardSecret());
        syncPoolStock(spuId);
        return vo;
    }

    /** 退款：已发放卡密作废，不再回到可售池。 */
    @Transactional
    public void revokeAfterRefund(String orderNo) {
        virtualCardMapper.releaseReservation(orderNo);
        List<VirtualCardEntity> issued = virtualCardMapper.selectList(
                Wrappers.<VirtualCardEntity>lambdaQuery()
                        .eq(VirtualCardEntity::getOrderNo, orderNo)
                        .eq(VirtualCardEntity::getStatus, VirtualCardEntity.ISSUED));
        if (issued.isEmpty()) {
            return;
        }
        virtualCardMapper.markRevokedByOrderNo(orderNo);
        issued.stream().map(VirtualCardEntity::getSpuId).distinct().forEach(this::syncPoolStock);
    }

    private void syncPoolStock(Long spuId) {
        if (spuId == null) {
            return;
        }
        int available = (int) Math.min(countAvailableBySpu(spuId), Integer.MAX_VALUE);
        productClient.syncSpuPoolStock(spuId, available);
    }

    private void assertVirtualSpu(Long spuId) {
        if (spuId == null) {
            throw BizException.of(ErrorCode.VIRTUAL_CARD_SPU_INVALID, "请选择虚拟商品");
        }
        productClient.assertVirtualSpu(spuId);
    }

    private static String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }
}
