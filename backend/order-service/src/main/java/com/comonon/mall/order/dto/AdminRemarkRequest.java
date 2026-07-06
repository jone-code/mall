package com.comonon.mall.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRemarkRequest {
    @Size(max = 512)
    private String remark;
}
