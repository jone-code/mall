package com.comonon.mall.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SyncSpuPoolRequest {
    @NotNull
    private Long spuId;
    @Min(0)
    private int available;
}
