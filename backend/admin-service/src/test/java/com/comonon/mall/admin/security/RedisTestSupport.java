package com.comonon.mall.admin.security;

import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 基于 Mockito + 内部 Map 的 Redis 替身。仅覆盖测试用到的子集，避免实现整个 RedisTemplate 接口。
 */
public final class RedisTestSupport {

    private RedisTestSupport() {}

    public static StringRedisTemplate fakeStringRedis() {
        final Map<String, String> kv = new ConcurrentHashMap<>();
        final Map<String, Map<Object, Object>> hashes = new ConcurrentHashMap<>();
        final Map<String, Set<String>> sets = new ConcurrentHashMap<>();

        StringRedisTemplate template = mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        @SuppressWarnings("unchecked")
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOps = mock(SetOperations.class);

        lenient().when(template.opsForValue()).thenReturn(valueOps);
        lenient().when(template.opsForHash()).thenReturn(hashOps);
        lenient().when(template.opsForSet()).thenReturn(setOps);

        lenient().when(template.hasKey(anyString())).thenAnswer((Answer<Boolean>) inv -> {
            String k = inv.getArgument(0);
            return kv.containsKey(k) || hashes.containsKey(k) || sets.containsKey(k);
        });
        lenient().when(template.delete(anyString())).thenAnswer((Answer<Boolean>) inv -> {
            String k = inv.getArgument(0);
            boolean a = kv.remove(k) != null;
            boolean b = hashes.remove(k) != null;
            boolean c = sets.remove(k) != null;
            return a || b || c;
        });
        lenient().when(template.expire(anyString(), any(java.time.Duration.class))).thenReturn(true);

        // ValueOperations
        lenient().doAnswer(inv -> { kv.put(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(valueOps).set(anyString(), anyString());
        lenient().doAnswer(inv -> { kv.put(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(valueOps).set(anyString(), anyString(), any(java.time.Duration.class));
        lenient().when(valueOps.get(any())).thenAnswer((Answer<String>) inv -> kv.get(inv.getArgument(0).toString()));
        lenient().when(valueOps.setIfAbsent(anyString(), anyString())).thenAnswer((Answer<Boolean>) inv ->
                kv.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);
        lenient().when(valueOps.increment(anyString())).thenAnswer((Answer<Long>) inv -> {
            String k = inv.getArgument(0);
            long cur = Long.parseLong(kv.getOrDefault(k, "0")) + 1;
            kv.put(k, Long.toString(cur));
            return cur;
        });
        lenient().when(valueOps.increment(anyString(), anyLong())).thenAnswer((Answer<Long>) inv -> {
            String k = inv.getArgument(0);
            long delta = inv.getArgument(1);
            long cur = Long.parseLong(kv.getOrDefault(k, "0")) + delta;
            kv.put(k, Long.toString(cur));
            return cur;
        });

        // HashOperations
        lenient().doAnswer(inv -> {
            String k = inv.getArgument(0);
            Map<?, ?> m = inv.getArgument(1);
            Map<Object, Object> bucket = hashes.computeIfAbsent(k, x -> new LinkedHashMap<>());
            m.forEach((mk, mv) -> bucket.put(mk, mv));
            return null;
        }).when(hashOps).putAll(anyString(), any(Map.class));
        lenient().doAnswer(inv -> {
            String k = inv.getArgument(0);
            hashes.computeIfAbsent(k, x -> new LinkedHashMap<>())
                    .put(inv.getArgument(1), inv.getArgument(2));
            return null;
        }).when(hashOps).put(anyString(), any(), any());
        lenient().when(hashOps.get(anyString(), any())).thenAnswer((Answer<Object>) inv -> {
            Map<Object, Object> m = hashes.get(inv.getArgument(0).toString());
            return m == null ? null : m.get(inv.getArgument(1));
        });
        lenient().when(hashOps.entries(anyString())).thenAnswer(inv -> {
            Map<Object, Object> m = hashes.get(inv.getArgument(0).toString());
            return m == null ? new LinkedHashMap<>() : new LinkedHashMap<>(m);
        });

        // SetOperations
        lenient().when(setOps.add(anyString(), any(String[].class))).thenAnswer((Answer<Long>) inv -> {
            String k = inv.getArgument(0);
            Object arg2 = inv.getArgument(1);
            String[] vs = arg2 instanceof String[] ? (String[]) arg2 : new String[] { (String) arg2 };
            Set<String> s = sets.computeIfAbsent(k, x -> new HashSet<>());
            long c = 0; for (String v : vs) if (s.add(v)) c++; return c;
        });
        lenient().when(setOps.members(anyString())).thenAnswer(inv -> {
            Set<String> s = sets.get(inv.getArgument(0).toString());
            return s == null ? new HashSet<>() : new HashSet<>(s);
        });
        lenient().when(setOps.remove(anyString(), any(Object[].class))).thenAnswer((Answer<Long>) inv -> {
            String k = inv.getArgument(0);
            Object[] vs = inv.getArgument(1);
            Set<String> s = sets.get(k);
            if (s == null) return 0L;
            long c = 0;
            for (Object v : vs) if (s.remove(v.toString())) c++;
            return c;
        });

        return template;
    }
}
