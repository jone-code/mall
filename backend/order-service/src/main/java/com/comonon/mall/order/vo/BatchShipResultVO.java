package com.comonon.mall.order.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchShipResultVO {
    private int success;
    private int failed;
    private List<String> errors = new ArrayList<>();
}
