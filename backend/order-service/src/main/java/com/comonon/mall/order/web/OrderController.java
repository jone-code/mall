package com.comonon.mall.order.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.dto.CreateOrderRequest;
import com.comonon.mall.order.dto.RefundApplyRequest;
import com.comonon.mall.order.security.OrderUserContext;
import com.comonon.mall.order.service.OrderService;
import com.comonon.mall.order.vo.CreateOrderResultVO;
import com.comonon.mall.order.vo.LogisticsVO;
import com.comonon.mall.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<CreateOrderResultVO> create(@Valid @RequestBody CreateOrderRequest req) {
        return Result.ok(orderService.create(OrderUserContext.getUserId(), req));
    }

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = OrderUserContext.getUserId();
        if (page != null) {
            return Result.ok(orderService.listByUserPage(userId, status, page, size));
        }
        return Result.ok(orderService.listByUser(userId, status));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.getDetail(OrderUserContext.getUserId(), orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelByUser(OrderUserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/confirm")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirmReceive(OrderUserContext.getUserId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/refund")
    public Result<Void> refund(@PathVariable String orderNo, @Valid @RequestBody RefundApplyRequest req) {
        orderService.applyRefund(OrderUserContext.getUserId(), orderNo, req.getReason());
        return Result.ok();
    }

    @GetMapping("/{orderNo}/logistics")
    public Result<LogisticsVO> logistics(@PathVariable String orderNo) {
        return Result.ok(orderService.getLogistics(OrderUserContext.getUserId(), orderNo));
    }
}
