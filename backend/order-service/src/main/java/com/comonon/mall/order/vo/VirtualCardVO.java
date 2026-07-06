package com.comonon.mall.order.vo;

import com.comonon.mall.order.entity.VirtualCardEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VirtualCardVO {
    private Long id;
    private Long spuId;
    private String cardNo;
    private String cardSecret;
    private String status;
    private String orderNo;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;

    public static VirtualCardVO from(VirtualCardEntity e) {
        VirtualCardVO vo = new VirtualCardVO();
        vo.setId(e.getId());
        vo.setSpuId(e.getSpuId());
        vo.setCardNo(e.getCardNo());
        vo.setCardSecret(e.getCardSecret());
        vo.setStatus(e.getStatus());
        vo.setOrderNo(e.getOrderNo());
        vo.setIssuedAt(e.getIssuedAt());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
