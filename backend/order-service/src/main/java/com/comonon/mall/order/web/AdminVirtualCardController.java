package com.comonon.mall.order.web;

import com.comonon.mall.common.web.PageResult;
import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.dto.ImportVirtualCardsRequest;
import com.comonon.mall.order.dto.UpdateVirtualCardRequest;
import com.comonon.mall.order.service.VirtualCardService;
import com.comonon.mall.order.vo.ImportCardsResultVO;
import com.comonon.mall.order.vo.VirtualCardPoolVO;
import com.comonon.mall.order.vo.VirtualCardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminVirtualCardController {

    private final VirtualCardService virtualCardService;

    @GetMapping("/virtual-cards")
    public Result<PageResult<VirtualCardVO>> list(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Long spuId,
                                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(virtualCardService.list(status, spuId, page, size));
    }

    @GetMapping("/virtual-cards/stats")
    public Result<Map<String, Long>> stats(@RequestParam(required = false) Long spuId) {
        long available = spuId != null
                ? virtualCardService.countAvailableBySpu(spuId)
                : virtualCardService.countAvailable();
        return Result.ok(Map.of("available", available));
    }

    @GetMapping("/virtual-cards/pool-summary")
    public Result<List<VirtualCardPoolVO>> poolSummary() {
        return Result.ok(virtualCardService.poolSummary());
    }

    @PostMapping("/virtual-cards/import")
    public Result<ImportCardsResultVO> importCards(@Valid @RequestBody ImportVirtualCardsRequest req) {
        return Result.ok(virtualCardService.importCards(req));
    }

    @PutMapping("/virtual-cards/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateVirtualCardRequest req) {
        virtualCardService.update(id, req);
        return Result.ok();
    }
}
