package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank
    @Size(max = 128)
    @Pattern(regexp = "^[a-z][a-z0-9_:]{2,127}$", message = "权限编码格式如 admin:role:write")
    private String code;

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String module;
}
