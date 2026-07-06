package com.comonon.mall.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.admin.entity.AdminUser;
import com.comonon.mall.admin.mapper.AdminUserMapper;
import com.comonon.mall.common.json.JsonMapperFactory;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 管理员账号 CRUD 与密码策略。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final AdminUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = JsonMapperFactory.create();

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    @Value("${mall.admin.password.min-length:10}")
    private int minLength;

    @Value("${mall.admin.password.history-keep:5}")
    private int historyKeep;

    private static final Pattern HAS_LOWER = Pattern.compile(".*[a-z]+.*");
    private static final Pattern HAS_UPPER = Pattern.compile(".*[A-Z]+.*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d+.*");
    private static final Pattern HAS_SYMBOL = Pattern.compile(".*[^a-zA-Z0-9]+.*");

    public AdminUser findByUsername(String username) {
        return userMapper.selectOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getUsername, username));
    }

    public AdminUser findById(Long id) { return userMapper.selectById(id); }

    public List<AdminUser> list(String keyword, int page, int size) {
        var w = Wrappers.<AdminUser>lambdaQuery();
        if (keyword != null && !keyword.isBlank()) {
            w.like(AdminUser::getUsername, keyword).or().like(AdminUser::getRealName, keyword);
        }
        w.orderByDesc(AdminUser::getId);
        w.last("LIMIT " + Math.max(0, (page - 1) * size) + "," + size);
        return userMapper.selectList(w);
    }

    @Transactional
    public AdminUser create(String username, String rawPassword, String realName, String phone, String email) {
        if (findByUsername(username) != null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "用户名已存在");
        }
        validatePolicy(rawPassword);
        AdminUser u = new AdminUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRealName(realName);
        u.setPhone(phone);
        u.setEmail(email);
        u.setStatus(0);
        u.setPwdUpdatedAt(LocalDateTime.now());
        u.setPwdHistory(serializeHistory(List.of(u.getPassword())));
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);
        return u;
    }

    /**
     * 修改密码。校验旧密码 + 不与最近 N 次重复。
     */
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        AdminUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "账号不存在");
        if (!passwordEncoder.matches(oldPassword, u.getPassword())) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "旧密码错误");
        }
        applyNewPassword(u, newPassword);
    }

    /**
     * 超级管理员重置他人密码（无需旧密码）。
     */
    @Transactional
    public void resetPasswordByAdmin(Long operatorId, List<String> operatorRoles,
                                     Long targetId, String newPassword) {
        if (operatorRoles == null || !operatorRoles.contains(SUPER_ADMIN_ROLE)) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "仅超级管理员可重置他人密码");
        }
        if (operatorId.equals(targetId)) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "请使用个人中心修改自己的密码");
        }
        AdminUser u = userMapper.selectById(targetId);
        if (u == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "账号不存在");
        }
        applyNewPassword(u, newPassword);
    }

    private void applyNewPassword(AdminUser u, String newPassword) {
        validatePolicy(newPassword);
        List<String> history = parseHistory(u.getPwdHistory());
        for (String h : history) {
            if (passwordEncoder.matches(newPassword, h)) {
                throw new BusinessException(ErrorCodes.PASSWORD_REUSED, "近 " + historyKeep + " 次密码不可复用");
            }
        }
        String newHash = passwordEncoder.encode(newPassword);
        LinkedList<String> updated = new LinkedList<>(history);
        updated.addFirst(newHash);
        while (updated.size() > historyKeep) {
            updated.removeLast();
        }
        u.setPassword(newHash);
        u.setPwdUpdatedAt(LocalDateTime.now());
        u.setPwdHistory(serializeHistory(updated));
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    public void validatePolicy(String pwd) {
        if (pwd == null || pwd.length() < minLength) {
            throw new BusinessException(ErrorCodes.PASSWORD_POLICY, "密码长度至少 " + minLength);
        }
        if (!HAS_LOWER.matcher(pwd).matches()
                || !HAS_UPPER.matcher(pwd).matches()
                || !HAS_DIGIT.matcher(pwd).matches()
                || !HAS_SYMBOL.matcher(pwd).matches()) {
            throw new BusinessException(ErrorCodes.PASSWORD_POLICY, "密码必须含大小写、数字、符号");
        }
    }

    public List<String> parseHistory(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serializeHistory(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    /** 更新最后登录信息。 */
    public void touchLogin(Long id, String ip) {
        AdminUser u = new AdminUser();
        u.setId(id);
        u.setLastLoginAt(LocalDateTime.now());
        u.setLastLoginIp(ip);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }
}
