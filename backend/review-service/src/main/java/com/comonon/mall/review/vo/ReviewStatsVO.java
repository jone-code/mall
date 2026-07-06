package com.comonon.mall.review.vo;

import lombok.Data;

@Data
public class ReviewStatsVO {
    /** 展示中的差评数（1-2 星） */
    private long badReviewCount;
    /** 展示中的评价总数 */
    private long visibleCount;
}
