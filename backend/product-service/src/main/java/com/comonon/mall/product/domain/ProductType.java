package com.comonon.mall.product.domain;

import java.util.Set;

public final class ProductType {
    private ProductType() {}

    public static final String PHYSICAL = "PHYSICAL";
    public static final String VIRTUAL = "VIRTUAL";
    public static final String SERVICE = "SERVICE";

    private static final Set<String> ALL = Set.of(PHYSICAL, VIRTUAL, SERVICE);

    public static boolean isValid(String type) {
        return type != null && ALL.contains(type);
    }
}
