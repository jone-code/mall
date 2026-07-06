package com.comonon.mall.product.dto;

import com.comonon.mall.product.domain.SpecJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateSpuRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String productType;
    @NotBlank
    @Size(min = 2, max = 128)
    private String title;
    private String subtitle;
    @NotBlank
    private String mainImage;
    private List<String> images;
    private String detailHtml;
    @Valid
    @NotNull
    private DefaultSkuPayload defaultSku;

    @Data
    public static class DefaultSkuPayload {
        @NotNull
        private SpecJson specJson;
        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal price;
        private BigDecimal marketPrice;
        private Integer available;
    }
}
