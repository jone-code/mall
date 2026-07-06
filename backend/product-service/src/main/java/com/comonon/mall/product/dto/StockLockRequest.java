package com.comonon.mall.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockLockRequest {

    @NotNull
    private String orderNo;

    @NotEmpty
    private List<Item> items;

    /** 锁定时长，默认 1800s。 */
    private Integer ttlSeconds;

    @Data
    public static class Item {
        @NotNull
        private Long skuId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
