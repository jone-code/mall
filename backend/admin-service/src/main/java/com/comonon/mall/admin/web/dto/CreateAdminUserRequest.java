package com.comonon.mall.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdminUserRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    private String realName;
    @NotBlank private String phone;
    private String email;
}
