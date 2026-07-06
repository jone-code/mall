package com.comonon.mall.product.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.product.dto.SkuSnapshotRequest;
import com.comonon.mall.product.service.SkuSnapshotService;
import com.comonon.mall.product.vo.SkuSnapshotListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/sku")
@RequiredArgsConstructor
public class InternalSkuController {

    private final SkuSnapshotService skuSnapshotService;

    @PostMapping("/snapshot")
    public Result<SkuSnapshotListVO> snapshot(@Valid @RequestBody SkuSnapshotRequest req) {
        return Result.ok(skuSnapshotService.snapshot(req.getSkuIds()));
    }
}
