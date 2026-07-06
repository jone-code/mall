package com.comonon.mall.pay.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.pay.security.PayUserContext;
import com.comonon.mall.pay.service.PayService;
import com.comonon.mall.pay.vo.CreatePayResultVO;
import com.comonon.mall.pay.vo.PayStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /** 创建/复用支付单。路径与 BFF /api/orders/{orderNo}/pay 对齐。 */
    @PostMapping("/orders/{orderNo}/pay")
    public Result<CreatePayResultVO> create(@PathVariable String orderNo) {
        return Result.ok(payService.create(PayUserContext.getUserId(), orderNo));
    }

    @PostMapping("/pay/{payNo}/confirm")
    public Result<PayStatusVO> confirm(@PathVariable String payNo) {
        return Result.ok(payService.confirm(PayUserContext.getUserId(), payNo));
    }

    @GetMapping("/pay/{payNo}")
    public Result<PayStatusVO> query(@PathVariable String payNo) {
        return Result.ok(payService.query(PayUserContext.getUserId(), payNo));
    }
}
