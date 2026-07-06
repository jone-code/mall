package com.comonon.mall.search.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.search.service.ProductSearchQueryService;
import com.comonon.mall.search.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductSearchQueryService searchService;

    @GetMapping("/products")
    public Result<PageResult<PageResult.ProductHit>> products(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(searchService.search(keyword, categoryId, page, size));
    }
}
