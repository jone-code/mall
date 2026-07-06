package com.comonon.mall.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_reviews")
public class ReviewEntity {
    public static final String VISIBLE = "VISIBLE";
    public static final String HIDDEN = "HIDDEN";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer rating;
    private String content;
    private String userNickname;
    private String status;
    private LocalDateTime createdAt;
}
