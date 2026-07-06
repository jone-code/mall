package com.comonon.mall.order.domain;

/**
 * 订单状态常量（见 order-design.md §4）。
 */
public final class OrderStatus {
    private OrderStatus() {}

    public static final String PENDING_PAY = "PENDING_PAY";
    public static final String PAID = "PAID";
    public static final String SHIPPED = "SHIPPED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    public static final String REFUNDING = "REFUNDING";
    public static final String REFUNDED = "REFUNDED";
}
