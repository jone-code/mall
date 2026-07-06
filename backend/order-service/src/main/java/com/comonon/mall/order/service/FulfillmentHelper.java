package com.comonon.mall.order.service;

import com.comonon.mall.common.json.JsonMapperFactory;
import com.comonon.mall.order.vo.FulfillmentVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

/** 履约信息序列化；虚拟卡密须从卡池分配，不再自动生成 Mock 卡密。 */
@Slf4j
public final class FulfillmentHelper {

    private static final ObjectMapper MAPPER = JsonMapperFactory.create();

    private FulfillmentHelper() {}

    public static FulfillmentVO virtualCard(String orderNo) {
        String suffix = orderNo.length() > 8 ? orderNo.substring(orderNo.length() - 8) : orderNo;
        FulfillmentVO vo = new FulfillmentVO();
        vo.setCardNo("VC-" + suffix.toUpperCase());
        vo.setCardSecret(randomDigits(16));
        return vo;
    }

    public static FulfillmentVO serviceCode(String orderNo) {
        FulfillmentVO vo = new FulfillmentVO();
        vo.setVerifyCode("SVC" + randomDigits(6));
        return vo;
    }

    public static String toJson(FulfillmentVO vo) {
        try {
            return MAPPER.writeValueAsString(vo);
        } catch (Exception e) {
            log.warn("serialize fulfillment failed", e);
            return "{}";
        }
    }

    public static FulfillmentVO fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, FulfillmentVO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }
}
