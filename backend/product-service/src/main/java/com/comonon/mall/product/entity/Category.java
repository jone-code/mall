package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String iconUrl;
    private Integer sortOrder;
    /** 0 禁用 / 1 启用 */
    private Integer status;
    private Integer level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
