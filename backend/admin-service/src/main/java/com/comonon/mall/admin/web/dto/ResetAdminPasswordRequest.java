package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetAdminPasswordRequest {
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
