package com.comonon.mall.product.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.product.service.SpuQueryService;
import com.comonon.mall.product.vo.PageResult;
import com.comonon.mall.product.vo.SpuIndexVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/search")
@RequiredArgsConstructor
public class InternalSearchController {

    private final SpuQueryService spuQueryService;

    @GetMapping("/on-sale")
    public Result<PageResult<SpuIndexVO>> onSale(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "100") int size) {
        return Result.ok(spuQueryService.listOnSaleForIndex(page, size));
    }

    @GetMapping("/spu/{id}")
    public Result<SpuIndexVO> spuForIndex(@PathVariable Long id) {
        return Result.ok(spuQueryService.indexDoc(id));
    }
}
