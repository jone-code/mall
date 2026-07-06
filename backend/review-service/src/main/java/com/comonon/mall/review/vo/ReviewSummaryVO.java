package com.comonon.mall.review.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewSummaryVO {
    private long count;
    private BigDecimal avgRating;
}
