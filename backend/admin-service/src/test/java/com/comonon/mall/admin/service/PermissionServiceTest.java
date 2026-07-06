package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminPermission;
import com.comonon.mall.admin.mapper.AdminPermissionMapper;
import com.comonon.mall.admin.mapper.AdminUserRoleMapper;
import com.comonon.mall.admin.security.RedisTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock AdminPermissionMapper permissionMapper;
    @Mock AdminUserRoleMapper userRoleMapper;
    StringRedisTemplate redis;
    PermissionService service;

    @BeforeEach
    void setUp() {
        redis = RedisTestSupport.fakeStringRedis();
        service = new PermissionService(permissionMapper, userRoleMapper, redis);
    }

    private AdminPermission p(String code) { AdminPermission ap = new AdminPermission(); ap.setCode(code); return ap; }

    @Test
    void permsHash_isStable_independentOfOrder() {
        when(permissionMapper.selectByAdminUserId(1L)).thenReturn(List.of(p("b"), p("a"), p("c")));
        when(permissionMapper.selectByAdminUserId(2L)).thenReturn(List.of(p("c"), p("a"), p("b")));
        String h1 = service.computePermsHash(service.loadPermissionCodes(1L));
        String h2 = service.computePermsHash(service.loadPermissionCodes(2L));
        assertEquals(h1, h2);
    }

    @Test
    void permsHash_changesWhenSetChanges() {
        String h1 = service.computePermsHash(List.of("a", "b"));
        String h2 = service.computePermsHash(List.of("a", "b", "c"));
        assertNotEquals(h1, h2);
    }

    @Test
    void bumpVersion_incrementsValue() {
        long v1 = service.bumpVersion(99L);
        long v2 = service.bumpVersion(99L);
        assertEquals(1L, v1);
        assertEquals(2L, v2);
    }
}
