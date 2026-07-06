package com.comonon.mall.cart.repository;

import com.comonon.mall.cart.domain.CartLine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CartRedisRepository {

    private static final String KEY_PREFIX = "cart:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<CartLine> findAll(Long userId) {
        String key = key(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartLine> lines = new ArrayList<>();
        for (Object value : entries.values()) {
            CartLine line = parse(String.valueOf(value));
            if (line != null) {
                lines.add(line);
            }
        }
        lines.sort(Comparator.comparing(CartLine::getAddedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return lines;
    }

    public CartLine findLine(Long userId, Long skuId) {
        String raw = (String) redisTemplate.opsForHash().get(key(userId), field(skuId));
        return raw == null ? null : parse(raw);
    }

    public int countLines(Long userId) {
        Long size = redisTemplate.opsForHash().size(key(userId));
        return size == null ? 0 : size.intValue();
    }

    public void saveLine(Long userId, CartLine line) {
        try {
            redisTemplate.opsForHash().put(key(userId), field(line.getSkuId()), objectMapper.writeValueAsString(line));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cart serialize failed", e);
        }
    }

    public void deleteLine(Long userId, Long skuId) {
        redisTemplate.opsForHash().delete(key(userId), field(skuId));
    }

    public void replaceAll(Long userId, List<CartLine> lines) {
        String key = key(userId);
        redisTemplate.delete(key);
        for (CartLine line : lines) {
            saveLine(userId, line);
        }
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    private String field(Long skuId) {
        return String.valueOf(skuId);
    }

    private CartLine parse(String json) {
        try {
            return objectMapper.readValue(json, CartLine.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public CartLine newLine(Long skuId, int quantity, boolean selected) {
        CartLine line = new CartLine();
        line.setSkuId(skuId);
        line.setQuantity(quantity);
        line.setSelected(selected);
        line.setAddedAt(LocalDateTime.now());
        return line;
    }
}
