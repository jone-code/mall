package com.comonon.mall.order.vo;

import com.comonon.mall.order.entity.ServiceVerifyCodeEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceVerifyCodeVO {
    private Long id;
    private Long spuId;
    private String verifyCode;
    private String status;
    private String orderNo;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;

    public static ServiceVerifyCodeVO from(ServiceVerifyCodeEntity e) {
        ServiceVerifyCodeVO vo = new ServiceVerifyCodeVO();
        vo.setId(e.getId());
        vo.setSpuId(e.getSpuId());
        vo.setVerifyCode(e.getVerifyCode());
        vo.setStatus(e.getStatus());
        vo.setOrderNo(e.getOrderNo());
        vo.setIssuedAt(e.getIssuedAt());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
