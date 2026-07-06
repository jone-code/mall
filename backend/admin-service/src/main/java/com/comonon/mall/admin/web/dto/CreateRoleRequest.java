package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,62}$", message = "编码需大写字母开头，仅含大写字母、数字、下划线")
    private String code;

    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 255)
    private String remark;
}
