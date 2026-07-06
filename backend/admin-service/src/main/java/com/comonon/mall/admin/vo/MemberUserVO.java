package com.comonon.mall.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberUserVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
