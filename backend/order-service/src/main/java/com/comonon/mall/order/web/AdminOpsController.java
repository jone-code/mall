package com.comonon.mall.order.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.order.service.OrderService;
import com.comonon.mall.order.vo.OpsTodoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ops")
@RequiredArgsConstructor
public class AdminOpsController {

    private final OrderService orderService;

    @GetMapping("/todos")
    public Result<OpsTodoVO> todos() {
        return Result.ok(orderService.opsTodos());
    }
}
