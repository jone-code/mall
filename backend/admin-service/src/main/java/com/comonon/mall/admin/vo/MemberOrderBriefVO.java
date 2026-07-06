package com.comonon.mall.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberOrderBriefVO {
    private String orderNo;
    private String status;
    private BigDecimal payAmount;
    private String createdAt;
}
