package com.comonon.mall.review.vo;

import com.comonon.mall.review.entity.ReviewEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer rating;
    private String content;
    private List<String> images = new ArrayList<>();
    private String userNickname;
    private String status;
    private LocalDateTime createdAt;

    public static ReviewVO from(ReviewEntity e) {
        ReviewVO vo = new ReviewVO();
        vo.setId(e.getId());
        vo.setOrderNo(e.getOrderNo());
        vo.setUserId(e.getUserId());
        vo.setSpuId(e.getSpuId());
        vo.setSkuId(e.getSkuId());
        vo.setRating(e.getRating());
        vo.setContent(e.getContent());
        vo.setUserNickname(e.getUserNickname());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
