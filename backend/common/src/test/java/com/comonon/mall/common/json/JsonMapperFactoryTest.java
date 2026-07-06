package com.comonon.mall.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JsonMapperFactoryTest {

    @Data
    static class SlimDto {
        private Long id;
        private String name;
    }

    @Data
    static class WithTime {
        private LocalDateTime createdAt;
    }

    @Test
    void ignoresUnknownFieldsWhenMappingPartialDto() throws Exception {
        ObjectMapper mapper = JsonMapperFactory.create();
        JsonNode node = mapper.readTree("""
                {"id":1,"name":"test","province":"北京","extra":true}
                """);
        SlimDto dto = mapper.treeToValue(node, SlimDto.class);
        assertEquals(1L, dto.getId());
        assertEquals("test", dto.getName());
    }

    @Test
    void serializesLocalDateTimeAsIsoString() throws Exception {
        ObjectMapper mapper = JsonMapperFactory.create();
        WithTime vo = new WithTime();
        vo.setCreatedAt(LocalDateTime.of(2026, 6, 27, 17, 54, 2));
        String json = mapper.writeValueAsString(vo);
        assertFalse(json.contains("["));
        assertEquals("{\"createdAt\":\"2026-06-27T17:54:02\"}", json);
    }
}
