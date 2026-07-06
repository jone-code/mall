package com.comonon.mall.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RemoveItemsRequest {
    @NotEmpty
    private List<Long> skuIds;
}
