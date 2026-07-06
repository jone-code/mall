package com.comonon.mall.product.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.product.service.CategoryService;
import com.comonon.mall.product.service.SpuQueryService;
import com.comonon.mall.product.vo.CategoryTreeVO;
import com.comonon.mall.product.vo.HomeVO;
import com.comonon.mall.product.vo.PageResult;
import com.comonon.mall.product.vo.SpuDetailVO;
import com.comonon.mall.product.vo.SpuSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductReadController {

    private final CategoryService categoryService;
    private final SpuQueryService spuQueryService;

    @GetMapping("/categories/tree")
    public Result<List<CategoryTreeVO>> categoryTree() {
        return Result.ok(categoryService.treeForConsumer());
    }

    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.ok(spuQueryService.home());
    }

    @GetMapping("/products")
    public Result<PageResult<SpuSummaryVO>> products(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(spuQueryService.listProducts(categoryId, keyword, page, size));
    }

    @GetMapping("/products/{id}")
    public Result<SpuDetailVO> productDetail(@PathVariable Long id) {
        return Result.ok(spuQueryService.detail(id));
    }
}
