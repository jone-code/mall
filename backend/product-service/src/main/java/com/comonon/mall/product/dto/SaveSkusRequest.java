package com.comonon.mall.product.dto;

import com.comonon.mall.product.domain.SpecJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaveSkusRequest {
    @NotEmpty
    @Valid
    private List<SkuItem> skus;

    @Data
    public static class SkuItem {
        private Long id;
        private String skuCode;
        @NotNull
        private SpecJson specJson;
        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal price;
        private BigDecimal marketPrice;
        private Integer isDefault;
        private Integer status;
        private Integer available;
    }
}
