package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sku_stock")
public class SkuStock {
    @TableId
    private Long skuId;
    private Integer available;
    private Integer frozen;
    private Integer version;
    private LocalDateTime updatedAt;
}
