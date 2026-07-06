package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 255)
    private String remark;
}
