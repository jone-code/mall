package com.comonon.mall.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @Min(0)
    private Integer quantity;
    private Boolean selected;
}
