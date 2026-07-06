package com.comonon.mall.review.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.review.dto.CreateReviewRequest;
import com.comonon.mall.review.security.ReviewUserContext;
import com.comonon.mall.review.service.ReviewService;
import com.comonon.mall.review.vo.PageResult;
import com.comonon.mall.review.vo.ReviewSummaryVO;
import com.comonon.mall.review.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<ReviewVO> create(@Valid @RequestBody CreateReviewRequest req,
                                   @RequestHeader(value = "X-User-Nickname", required = false) String nickname) {
        return Result.ok(reviewService.create(ReviewUserContext.getUserId(), req, nickname));
    }

    @GetMapping("/me")
    public Result<List<ReviewVO>> mine() {
        return Result.ok(reviewService.listMine(ReviewUserContext.getUserId()));
    }

    @GetMapping("/order/{orderNo}")
    public Result<ReviewVO> byOrder(@PathVariable String orderNo) {
        return Result.ok(reviewService.getByOrderNoForUser(ReviewUserContext.getUserId(), orderNo));
    }

    @GetMapping("/spu/{spuId}")
    public Result<PageResult<ReviewVO>> bySpu(@PathVariable Long spuId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reviewService.listBySpu(spuId, page, size));
    }

    @GetMapping("/spu/{spuId}/summary")
    public Result<ReviewSummaryVO> summary(@PathVariable Long spuId) {
        return Result.ok(reviewService.summaryBySpu(spuId));
    }
}
