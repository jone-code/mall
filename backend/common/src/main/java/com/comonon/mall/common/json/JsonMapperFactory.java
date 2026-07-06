package com.comonon.mall.common.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 统一的 Jackson 配置，供各微服务注入 {@link ObjectMapper} Bean。
 *
 * <p>服务间 HTTP 调用常见模式：内部接口返回完整 VO，调用方只映射精简 DTO。
 * 必须忽略未知字段，否则 {@code treeToValue} 会失败并被误报为「不存在」等业务错误。
 *
 * <p>日期时间统一序列化为 ISO-8601 字符串（如 {@code 2026-06-27T17:54:02}），
 * 避免 {@code LocalDateTime} 被写成 {@code [2026,6,27,17,54,2]} 数组。
 */
public final class JsonMapperFactory {

    private JsonMapperFactory() {}

    public static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
