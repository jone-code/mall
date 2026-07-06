package com.comonon.mall.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 1000)
    private String content;
}
