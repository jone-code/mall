package com.comonon.mall.order.web;

import com.comonon.mall.common.web.PageResult;
import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.dto.ImportServiceVerifyCodesRequest;
import com.comonon.mall.order.dto.UpdateServiceVerifyCodeRequest;
import com.comonon.mall.order.dto.VerifyServiceRequest;
import com.comonon.mall.order.service.OrderService;
import com.comonon.mall.order.service.ServiceVerifyCodeService;
import com.comonon.mall.order.vo.OrderVO;
import com.comonon.mall.order.vo.ServiceVerifyCodeVO;
import jakarta.servlet.http.HttpServletRequest;
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
public class AdminServiceVerifyCodeController {

    private final ServiceVerifyCodeService serviceVerifyCodeService;
    private final OrderService orderService;

    @GetMapping("/service-verify-codes")
    public Result<PageResult<ServiceVerifyCodeVO>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false) Long spuId,
                                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(serviceVerifyCodeService.list(status, spuId, page, size));
    }

    @GetMapping("/service-verify-codes/stats")
    public Result<Map<String, Long>> stats(@RequestParam(required = false) Long spuId) {
        long available = spuId != null
                ? serviceVerifyCodeService.countAvailableBySpu(spuId)
                : serviceVerifyCodeService.countAvailable();
        return Result.ok(Map.of("available", available));
    }

    @PostMapping("/service-verify-codes/import")
    public Result<Map<String, Integer>> importCodes(@Valid @RequestBody ImportServiceVerifyCodesRequest req) {
        return Result.ok(Map.of("imported", serviceVerifyCodeService.importCodes(req)));
    }

    @PutMapping("/service-verify-codes/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateServiceVerifyCodeRequest req) {
        serviceVerifyCodeService.update(id, req);
        return Result.ok();
    }

    @PostMapping("/service/verify")
    public Result<OrderVO> verify(@Valid @RequestBody VerifyServiceRequest req, HttpServletRequest request) {
        return Result.ok(orderService.verifyService(req.getVerifyCode(),
                request.getHeader("X-Admin-User-Id")));
    }
}
