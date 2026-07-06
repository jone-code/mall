package com.comonon.mall.order.vo;

import com.comonon.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long skuId;
    private Long spuId;
    private String title;
    private String specText;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String productType;

    public static OrderItemVO from(OrderItemEntity e) {
        OrderItemVO vo = new OrderItemVO();
        vo.setSkuId(e.getSkuId());
        vo.setSpuId(e.getSpuId());
        vo.setTitle(e.getTitle());
        vo.setSpecText(e.getSpecText());
        vo.setMainImage(e.getMainImage());
        vo.setPrice(e.getPrice());
        vo.setQuantity(e.getQuantity());
        vo.setSubtotal(e.getSubtotal());
        vo.setProductType(e.getProductType());
        return vo;
    }
}
