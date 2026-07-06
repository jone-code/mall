package com.comonon.mall.pay.vo;

import com.comonon.mall.pay.entity.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayStatusVO {
    private String payNo;
    private String orderNo;
    private String status;
    private BigDecimal amount;
    private String channel;
    private String channelTxn;
    private LocalDateTime paidAt;

    public static PayStatusVO from(Payment p) {
        PayStatusVO vo = new PayStatusVO();
        vo.setPayNo(p.getPayNo());
        vo.setOrderNo(p.getOrderNo());
        vo.setStatus(p.getStatus());
        vo.setAmount(p.getAmount());
        vo.setChannel(p.getChannel());
        vo.setChannelTxn(p.getChannelTxn());
        vo.setPaidAt(p.getPaidAt());
        return vo;
    }
}
