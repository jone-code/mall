package com.comonon.mall.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogisticsEventVO {
    private LocalDateTime time;
    private String status;
    private String desc;
}
