package com.comonon.mall.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchProductStatusRequest {
    @NotEmpty(message = "请选择商品")
    private List<Long> ids;

    /** publish | offline */
    @NotNull
    private String action;
}
