package com.comonon.mall.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 16, message = "昵称长度 2-16 个字符")
    private String nickname;

    /** 头像 URL；传空字符串表示清除 */
    private String avatarUrl;
}
