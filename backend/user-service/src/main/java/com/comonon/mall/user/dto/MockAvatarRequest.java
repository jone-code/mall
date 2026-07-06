package com.comonon.mall.user.dto;

import lombok.Data;

@Data
public class MockAvatarRequest {
    /** 可选：客户端本地路径，Mock 模式下仅用于生成 seed */
    private String localPath;
}
