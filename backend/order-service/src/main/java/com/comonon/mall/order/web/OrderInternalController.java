package com.comonon.mall.order.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.service.OrderService;
import com.comonon.mall.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部订单接口，供 pay-service 调用。
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.adminDetail(orderNo));
    }

    @PostMapping("/{orderNo}/paid")
    public Result<Void> markPaid(@PathVariable String orderNo) {
        orderService.markPaid(orderNo);
        return Result.ok();
    }

    @GetMapping("/{orderNo}/status")
    public Result<String> status(@PathVariable String orderNo) {
        return Result.ok(orderService.getStatus(orderNo));
    }
}
