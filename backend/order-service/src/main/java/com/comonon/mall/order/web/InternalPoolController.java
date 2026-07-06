package com.comonon.mall.order.service;

import com.comonon.mall.common.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 内部码池查询，供 BFF 商品详情展示。 */
@RestController
@RequestMapping("/internal/pool")
@RequiredArgsConstructor
public class InternalPoolController {

    private final VirtualCardService virtualCardService;
    private final ServiceVerifyCodeService serviceVerifyCodeService;

    @GetMapping("/available")
    public Result<Map<String, Long>> available(@RequestParam Long spuId,
                                               @RequestParam String productType) {
        long count = switch (productType) {
            case "VIRTUAL" -> virtualCardService.countAvailableBySpu(spuId);
            case "SERVICE" -> serviceVerifyCodeService.countAvailableBySpu(spuId);
            default -> 0L;
        };
        return Result.ok(Map.of("available", count));
    }
}
