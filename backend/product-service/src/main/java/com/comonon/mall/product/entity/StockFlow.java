package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_flow")
public class StockFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private String orderNo;
    private String changeType;
    private Integer deltaAvailable;
    private Integer deltaFrozen;
    private Integer availableAfter;
    private Integer frozenAfter;
    private String operatorType;
    private String operatorId;
    private String remark;
    private LocalDateTime createdAt;
}
