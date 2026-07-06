package com.comonon.mall.pay.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.pay.service.PayService;
import com.comonon.mall.pay.vo.PayStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 内部支付接口，供 order-service 等调用。 */
@RestController
@RequestMapping("/internal/pay")
@RequiredArgsConstructor
public class PayInternalController {

    private final PayService payService;

    @GetMapping("/order/{orderNo}")
    public Result<PayStatusVO> byOrder(@PathVariable String orderNo) {
        PayStatusVO vo = payService.getSuccessByOrderNo(orderNo);
        return Result.ok(vo);
    }

    @PostMapping("/order/{orderNo}/refund")
    public Result<Void> refund(@PathVariable String orderNo) {
        payService.mockRefundByOrderNo(orderNo);
        return Result.ok();
    }
}
