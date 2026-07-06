package com.comonon.mall.review.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list = new ArrayList<>();
    private int page;
    private int size;
    private long total;

    public static <T> PageResult<T> of(List<T> list, int page, int size, long total) {
        PageResult<T> r = new PageResult<>();
        r.list = list;
        r.page = page;
        r.size = size;
        r.total = total;
        return r;
    }
}
