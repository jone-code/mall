package com.comonon.mall.admin.web;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.admin.entity.AdminUser;
import com.comonon.mall.admin.entity.AdminUserRole;
import com.comonon.mall.admin.mapper.AdminUserRoleMapper;
import com.comonon.mall.admin.security.AdminContext;
import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.RequirePermission;
import com.comonon.mall.admin.service.AdminTokenService;
import com.comonon.mall.admin.service.AdminUserService;
import com.comonon.mall.admin.service.PermissionService;
import com.comonon.mall.admin.web.dto.AssignRolesRequest;
import com.comonon.mall.admin.web.dto.CreateAdminUserRequest;
import com.comonon.mall.admin.web.dto.ResetAdminPasswordRequest;
import com.comonon.mall.common.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserRoleMapper userRoleMapper;
    private final PermissionService permissionService;
    private final AdminTokenService tokenService;

    @GetMapping
    @RequirePermission("admin:user:read")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<AdminUser> users = adminUserService.list(keyword, page, size);
        List<Map<String, Object>> rows = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("status", u.getStatus());
            m.put("lastLoginAt", u.getLastLoginAt());
            return m;
        }).toList();
        return Result.ok(rows);
    }

    @GetMapping("/{id}/roles")
    @RequirePermission("admin:user:read")
    public Result<List<Long>> getUserRoles(@PathVariable("id") Long id) {
        List<Long> roleIds = userRoleMapper.selectList(
                        Wrappers.<AdminUserRole>lambdaQuery().eq(AdminUserRole::getAdminUserId, id))
                .stream().map(AdminUserRole::getRoleId).toList();
        return Result.ok(roleIds);
    }

    @PostMapping
    @RequirePermission("admin:user:create")
    @AuditAction(value = "CREATE_ADMIN_USER", targetType = "admin_user")
    public Result<Long> create(@Valid @RequestBody CreateAdminUserRequest req) {
        AdminUser u = adminUserService.create(req.getUsername(), req.getPassword(),
                req.getRealName(), req.getPhone(), req.getEmail());
        return Result.ok(u.getId());
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("admin:role:assign")
    @AuditAction(value = "ASSIGN_ROLES", targetType = "admin_user")
    @Transactional
    public Result<Void> assignRoles(@PathVariable("id") Long id, @Valid @RequestBody AssignRolesRequest req) {
        userRoleMapper.delete(Wrappers.<AdminUserRole>lambdaQuery().eq(AdminUserRole::getAdminUserId, id));
        for (Long roleId : req.getRoleIds()) {
            AdminUserRole ur = new AdminUserRole();
            ur.setAdminUserId(id);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        permissionService.bumpVersion(id);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @RequirePermission("admin:user:write")
    @AuditAction(value = "RESET_ADMIN_PASSWORD", targetType = "admin_user")
    public Result<Void> resetPassword(@PathVariable("id") Long id,
                                      @Valid @RequestBody ResetAdminPasswordRequest req) {
        AdminContext.Holder ctx = AdminContext.get();
        adminUserService.resetPasswordByAdmin(
                ctx.adminUserId(), ctx.roles(), id, req.getNewPassword());
        tokenService.revokeAllSessions(id);
        permissionService.bumpVersion(id);
        return Result.ok();
    }
}
