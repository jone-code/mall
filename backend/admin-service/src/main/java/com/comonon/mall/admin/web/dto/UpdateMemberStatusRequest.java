package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberStatusRequest {
    @NotNull
    @Min(0)
    @Max(2)
    private Integer status;

    /** 变更原因（可选，写入审计 requestBody） */
    @Size(max = 200)
    private String reason;
}
