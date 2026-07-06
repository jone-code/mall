package com.comonon.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_verify_codes")
public class ServiceVerifyCodeEntity {
    public static final String AVAILABLE = "AVAILABLE";
    public static final String RESERVED = "RESERVED";
    public static final String ISSUED = "ISSUED";
    public static final String REVOKED = "REVOKED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String verifyCode;
    private String status;
    private String orderNo;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
}
