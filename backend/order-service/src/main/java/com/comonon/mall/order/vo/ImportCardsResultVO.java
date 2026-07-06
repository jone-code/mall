package com.comonon.mall.order.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportCardsResultVO {
    private int imported;
    private int duplicate;
    private int skipped;
    private List<String> errors = new ArrayList<>();
}
