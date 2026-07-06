package com.comonon.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台主控台订单统计。
 */
@Data
public class OrderStatsVO {
    /** 订单总数 */
    private long totalOrders;
    private long pendingPay;
    private long paid;
    private long shipped;
    private long completed;
    private long cancelled;
    private long refunding;
    private long refunded;

    /** 今日新增订单数（按下单时间） */
    private long todayOrders;
    /** 今日已支付 GMV（按支付时间） */
    private BigDecimal todayGmv = BigDecimal.ZERO;
    /** 累计已支付 GMV */
    private BigDecimal totalGmv = BigDecimal.ZERO;
}
