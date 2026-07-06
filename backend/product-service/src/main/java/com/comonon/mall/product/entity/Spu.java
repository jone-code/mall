package com.comonon.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("spu")
public class Spu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String title;
    private String subtitle;
    private String productType;
    private String mainImage;
    /** JSON 数组字符串 */
    private String images;
    private String detailHtml;
    /** 0 草稿 / 1 上架 / 2 下架 */
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
