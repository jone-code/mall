package com.comonon.mall.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectAllRequest {
    @NotNull
    private Boolean selected;
}
