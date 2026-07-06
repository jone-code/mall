package com.comonon.mall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class LogisticsVO {
    private String trackingNo;
    private String trackingCompany;
    private String status;
    private List<LogisticsEventVO> events;
}
