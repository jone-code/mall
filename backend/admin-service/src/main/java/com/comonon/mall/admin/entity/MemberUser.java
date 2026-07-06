package com.comonon.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** C 端会员用户，映射表 {@code user}。 */
@Data
@TableName("user")
public class MemberUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nickname;
    private String avatarUrl;
    /** 0 正常 / 1 禁用 / 2 注销 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
