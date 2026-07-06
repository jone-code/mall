package com.comonon.mall.review.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.review.service.ReviewService;
import com.comonon.mall.review.vo.PageResult;
import com.comonon.mall.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public Result<PageResult<ReviewVO>> list(@RequestParam(required = false) Long spuId,
                                             @RequestParam(required = false) Integer rating,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) LocalDate from,
                                             @RequestParam(required = false) LocalDate to,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return Result.ok(reviewService.adminList(spuId, rating, status, from, to, page, size));
    }

    @PostMapping("/{id}/hide")
    public Result<Void> hide(@PathVariable Long id) {
        reviewService.hide(id);
        return Result.ok();
    }

    @PostMapping("/{id}/unhide")
    public Result<Void> unhide(@PathVariable Long id) {
        reviewService.unhide(id);
        return Result.ok();
    }
}
