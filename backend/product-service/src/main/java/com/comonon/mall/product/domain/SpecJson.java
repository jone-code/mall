package com.comonon.mall.product.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpecJson {
    private List<SpecDim> dims = new ArrayList<>();

    @Data
    public static class SpecDim {
        private String name;
        private String value;
    }
}
