package com.comonon.mall.review.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderBriefDto {
    private String orderNo;
    private Long userId;
    private String status;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long spuId;
        private Long skuId;
        private String title;
    }
}
