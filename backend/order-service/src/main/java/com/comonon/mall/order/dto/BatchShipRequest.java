package com.comonon.mall.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchShipRequest {
    @NotEmpty
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotBlank
        private String orderNo;
        @NotBlank
        private String trackingNo;
        private String trackingCompany;
    }
}
