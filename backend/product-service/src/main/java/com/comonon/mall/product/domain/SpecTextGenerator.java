package com.comonon.mall.product.domain;

import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class SpecTextGenerator {
    private SpecTextGenerator() {}

    public static String generate(SpecJson spec) {
        if (spec == null || spec.getDims() == null || spec.getDims().isEmpty()) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "规格不能为空");
        }
        List<SpecJson.SpecDim> dims = spec.getDims().stream()
                .map(d -> {
                    SpecJson.SpecDim dim = new SpecJson.SpecDim();
                    dim.setName(trim(d.getName(), "规格名"));
                    dim.setValue(trim(d.getValue(), "规格值"));
                    return dim;
                })
                .sorted(Comparator.comparing(SpecJson.SpecDim::getName))
                .collect(Collectors.toList());
        return dims.stream().map(SpecJson.SpecDim::getValue).collect(Collectors.joining(" / "));
    }

    private static String trim(String s, String label) {
        if (s == null) {
            throw BizException.of(ErrorCode.BAD_REQUEST, label + "不能为空");
        }
        String t = s.trim();
        if (t.isEmpty() || t.length() > 32) {
            throw BizException.of(ErrorCode.BAD_REQUEST, label + "长度须 1~32");
        }
        return t;
    }
}
