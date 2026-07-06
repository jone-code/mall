package com.comonon.mall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItemEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long skuId;
    private Long spuId;
    private String title;
    private String specText;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String productType;
}
