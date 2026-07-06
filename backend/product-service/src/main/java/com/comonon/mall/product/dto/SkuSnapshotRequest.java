package com.comonon.mall.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SkuSnapshotRequest {
    @NotEmpty
    private List<Long> skuIds;
}
