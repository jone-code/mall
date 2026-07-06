package com.comonon.mall.product.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.product.dto.CreateCategoryRequest;
import com.comonon.mall.product.dto.UpdateCategoryRequest;
import com.comonon.mall.product.service.CategoryService;
import com.comonon.mall.product.vo.CategoryTreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryTreeVO>> list() {
        return Result.ok(categoryService.treeForAdmin());
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody CreateCategoryRequest req) {
        Long id = categoryService.create(req);
        return Result.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest req) {
        categoryService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
