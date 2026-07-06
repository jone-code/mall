package com.comonon.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderTrendVO {
    private int days;
    private List<DailyPoint> points = new ArrayList<>();

    @Data
    public static class DailyPoint {
        private String date;
        private long orderCount;
        private BigDecimal gmv = BigDecimal.ZERO;
    }
}
