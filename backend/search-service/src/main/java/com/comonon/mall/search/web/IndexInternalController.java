package com.comonon.mall.search.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.search.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/index")
@RequiredArgsConstructor
public class IndexInternalController {

    private final ProductIndexService indexService;

    @PutMapping("/spu/{id}")
    public Result<Void> indexSpu(@PathVariable Long id) {
        indexService.indexSpu(id);
        return Result.ok();
    }

    @DeleteMapping("/spu/{id}")
    public Result<Void> deleteSpu(@PathVariable Long id) {
        indexService.deleteSpu(id);
        return Result.ok();
    }

    @PostMapping("/rebuild")
    public Result<Map<String, Integer>> rebuild() {
        return Result.ok(Map.of("indexed", indexService.rebuildAll()));
    }
}
