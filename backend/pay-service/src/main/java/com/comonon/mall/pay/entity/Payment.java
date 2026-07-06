package com.comonon.mall.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String CLOSED = "CLOSED";
    public static final String REFUNDED = "REFUNDED";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String payNo;
    private String orderNo;
    private Long userId;
    private String channel;
    private BigDecimal amount;
    private String status;
    private String channelTxn;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
