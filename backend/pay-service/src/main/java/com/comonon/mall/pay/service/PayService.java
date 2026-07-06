package com.comonon.mall.pay.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.pay.client.OrderClient;
import com.comonon.mall.pay.client.dto.OrderInfoDto;
import com.comonon.mall.pay.entity.Payment;
import com.comonon.mall.pay.mapper.PaymentMapper;
import com.comonon.mall.pay.vo.CreatePayResultVO;
import com.comonon.mall.pay.vo.PayStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayService {

    private static final String CHANNEL_MOCK = "MOCK";
    private static final String ORDER_PENDING_PAY = "PENDING_PAY";

    private final PaymentMapper paymentMapper;
    private final PayNoGenerator payNoGenerator;
    private final OrderClient orderClient;

    /** 创建（或复用）支付单。金额以订单应付金额为准。 */
    public CreatePayResultVO create(Long userId, String orderNo) {
        OrderInfoDto order = orderClient.getOrder(orderNo);
        if (!userId.equals(order.getUserId())) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!ORDER_PENDING_PAY.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "订单当前不可支付");
        }

        Payment existing = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, orderNo)
                .eq(Payment::getStatus, Payment.PENDING)
                .last("LIMIT 1"));
        if (existing != null) {
            return toCreateResult(existing, orderNo);
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setPayNo(payNoGenerator.next());
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setChannel(CHANNEL_MOCK);
        payment.setAmount(order.getPayAmount());
        payment.setStatus(Payment.PENDING);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        try {
            paymentMapper.insert(payment);
        } catch (DuplicateKeyException e) {
            Payment raced = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                    .eq(Payment::getOrderNo, orderNo)
                    .eq(Payment::getStatus, Payment.PENDING)
                    .last("LIMIT 1"));
            if (raced != null) {
                return toCreateResult(raced, orderNo);
            }
            throw e;
        }
        return toCreateResult(payment, orderNo);
    }

    /**
     * Mock 支付确认：先通知订单已支付，成功后再置支付单 SUCCESS，避免支付成功但订单已取消。
     */
    public PayStatusVO confirm(Long userId, String payNo) {
        Payment payment = getOwned(userId, payNo);
        if (Payment.SUCCESS.equals(payment.getStatus())) {
            orderClient.notifyPaid(payment.getOrderNo());
            return PayStatusVO.from(payment);
        }
        if (!Payment.PENDING.equals(payment.getStatus())) {
            throw BizException.of(ErrorCode.PAYMENT_STATE_ILLEGAL, "支付单状态不允许该操作");
        }

        OrderInfoDto order = orderClient.getOrder(payment.getOrderNo());
        if (!userId.equals(order.getUserId())) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!ORDER_PENDING_PAY.equals(order.getStatus())) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "订单当前不可支付");
        }
        if (payment.getAmount() != null && order.getPayAmount() != null
                && payment.getAmount().compareTo(order.getPayAmount()) != 0) {
            throw BizException.of(ErrorCode.ORDER_STATUS_ILLEGAL, "订单金额已变更，请重新发起支付");
        }

        // 先原子更新订单，再标记支付成功
        orderClient.notifyPaid(payment.getOrderNo());

        String txn = "MOCK-" + System.currentTimeMillis();
        int n = paymentMapper.markSuccess(payNo, txn, LocalDateTime.now());
        if (n == 0) {
            payment = getOwned(userId, payNo);
            if (!Payment.SUCCESS.equals(payment.getStatus())) {
                throw BizException.of(ErrorCode.PAYMENT_STATE_ILLEGAL, "支付单状态不允许该操作");
            }
        }
        return PayStatusVO.from(getOwned(userId, payNo));
    }

    public PayStatusVO query(Long userId, String payNo) {
        return PayStatusVO.from(getOwned(userId, payNo));
    }

    /** 内部：按订单号查成功支付单。 */
    public PayStatusVO getSuccessByOrderNo(String orderNo) {
        Payment p = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, orderNo)
                .eq(Payment::getStatus, Payment.SUCCESS)
                .orderByDesc(Payment::getId)
                .last("LIMIT 1"));
        return p == null ? null : PayStatusVO.from(p);
    }

    /** Mock 退款：将 SUCCESS 支付单置为 REFUNDED（幂等）。 */
    public void mockRefundByOrderNo(String orderNo) {
        int n = paymentMapper.markRefundedByOrderNo(orderNo);
        if (n == 0) {
            throw BizException.of(ErrorCode.PAYMENT_STATE_ILLEGAL, "支付单退款失败或不存在");
        }
        log.info("mock refund orderNo={}", orderNo);
    }

    private CreatePayResultVO toCreateResult(Payment payment, String orderNo) {
        CreatePayResultVO vo = new CreatePayResultVO();
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(orderNo);
        vo.setChannel(payment.getChannel());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setMockConfirmUrl("/api/pay/" + payment.getPayNo() + "/confirm");
        return vo;
    }

    private Payment getOwned(Long userId, String payNo) {
        Payment p = paymentMapper.selectOne(Wrappers.<Payment>lambdaQuery().eq(Payment::getPayNo, payNo));
        if (p == null || !userId.equals(p.getUserId())) {
            throw BizException.of(ErrorCode.PAYMENT_NOT_FOUND, "支付单不存在");
        }
        return p;
    }
}
