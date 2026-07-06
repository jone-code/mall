package com.comonon.mall.product.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeVO {
    private List<CategoryTreeVO> categories = new ArrayList<>();
    private List<SpuSummaryVO> recommendSpus = new ArrayList<>();
}
