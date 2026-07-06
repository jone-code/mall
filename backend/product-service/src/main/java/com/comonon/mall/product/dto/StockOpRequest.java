package com.comonon.mall.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockOpRequest {

    @NotNull
    private String orderNo;

    /** 释放原因：USER_CANCEL/TIMEOUT/ADMIN_CLOSE 等。 */
    private String reason;
}
