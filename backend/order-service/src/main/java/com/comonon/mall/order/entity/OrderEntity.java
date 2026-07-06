package com.comonon.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class OrderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private Integer itemCount;
    private String productType;
    private String receiver;
    private String receiverPhone;
    private String addressDetail;
    private LocalDateTime expireAt;
    private LocalDateTime payAt;
    private LocalDateTime shipAt;
    private String trackingNo;
    private String trackingCompany;
    private LocalDateTime completeAt;
    private String fulfillmentJson;
    private LocalDateTime cancelAt;
    private String cancelReason;
    private String remark;
    private String adminRemark;
    private LocalDateTime refundAt;
    private String refundReason;
    private String refundFromStatus;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
