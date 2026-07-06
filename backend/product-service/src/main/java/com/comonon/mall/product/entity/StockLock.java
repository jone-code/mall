package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_lock")
public class StockLock {

    public static final int LOCKED = 1;
    public static final int RELEASED = 2;
    public static final int DEDUCTED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long skuId;
    private Integer quantity;
    private Integer status;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
