package com.comonon.mall.admin.service;

import com.comonon.mall.admin.entity.AdminPermission;
import com.comonon.mall.admin.mapper.AdminPermissionMapper;
import com.comonon.mall.admin.mapper.AdminUserRoleMapper;
import com.comonon.mall.common.security.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.TreeSet;

/**
 * 管理员权限聚合：查 permission code、计算 permsHash、维护 permsVersion。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final AdminPermissionMapper permissionMapper;
    private final AdminUserRoleMapper userRoleMapper;
    private final StringRedisTemplate redis;

    /** 查询用户所有 permission code（去重排序）。 */
    public List<String> loadPermissionCodes(Long adminUserId) {
        List<AdminPermission> permissions = permissionMapper.selectByAdminUserId(adminUserId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream().map(AdminPermission::getCode).distinct().sorted().toList();
    }

    public List<String> loadRoleCodes(Long adminUserId) {
        List<String> roles = userRoleMapper.selectRoleCodes(adminUserId);
        return roles == null ? Collections.emptyList() : roles;
    }

    /**
     * 基于权限码集合计算 SHA-256 指纹（去重 + 排序后 join），用于 JWT permsHash。
     */
    public String computePermsHash(List<String> codes) {
        TreeSet<String> sorted = new TreeSet<>(codes == null ? Collections.emptyList() : codes);
        String joined = String.join(",", sorted);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(joined.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 当前版本（不存在视为 0）。 */
    public long currentVersion(Long adminUserId) {
        String v = redis.opsForValue().get(RedisKeys.adminPermsVersion(adminUserId));
        return v == null ? 0L : Long.parseLong(v);
    }

    /** 触发权限变更：版本 +1，相关用户旧 access 在拦截器侧检测到不一致即被拒。 */
    public long bumpVersion(Long adminUserId) {
        Long val = redis.opsForValue().increment(RedisKeys.adminPermsVersion(adminUserId));
        return val == null ? 0L : val;
    }
}
