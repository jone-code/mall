package com.comonon.mall.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_identity")
public class UserIdentity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** PHONE / WECHAT */
    private String identityType;
    private String identifier;
    private String unionId;
    private Integer verified;
    private LocalDateTime createdAt;

    public static final String TYPE_PHONE = "PHONE";
    public static final String TYPE_WECHAT = "WECHAT";
}
