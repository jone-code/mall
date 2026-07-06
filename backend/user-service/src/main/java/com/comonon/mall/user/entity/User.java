package com.comonon.mall.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String avatarUrl;
    /** 0 正常 / 1 禁用 / 2 注销 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
