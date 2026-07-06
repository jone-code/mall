package com.comonon.mall.admin.web;

import com.comonon.mall.admin.entity.AdminPermission;
import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.RequirePermission;
import com.comonon.mall.admin.service.AdminRbacService;
import com.comonon.mall.admin.vo.RoleVO;
import com.comonon.mall.admin.web.dto.AssignPermissionsRequest;
import com.comonon.mall.admin.web.dto.CreatePermissionRequest;
import com.comonon.mall.admin.web.dto.CreateRoleRequest;
import com.comonon.mall.admin.web.dto.UpdatePermissionRequest;
import com.comonon.mall.admin.web.dto.UpdateRoleRequest;
import com.comonon.mall.common.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/rbac")
@RequiredArgsConstructor
public class AdminRbacController {

    private final AdminRbacService rbacService;

    @GetMapping("/permissions")
    @RequirePermission("admin:permission:read")
    public Result<List<Map<String, Object>>> listPermissions() {
        List<Map<String, Object>> rows = rbacService.listPermissions().stream().map(p -> toPermissionMap(p)).toList();
        return Result.ok(rows);
    }

    @PostMapping("/permissions")
    @RequirePermission("admin:permission:write")
    @AuditAction(value = "CREATE_PERMISSION", targetType = "admin_permission")
    public Result<Map<String, Object>> createPermission(@Valid @RequestBody CreatePermissionRequest req) {
        AdminPermission p = rbacService.createPermission(req.getCode(), req.getName(), req.getModule());
        return Result.ok(toPermissionMap(p));
    }

    @PutMapping("/permissions/{id}")
    @RequirePermission("admin:permission:write")
    @AuditAction(value = "UPDATE_PERMISSION", targetType = "admin_permission")
    public Result<Map<String, Object>> updatePermission(@PathVariable("id") Long id,
                                                        @Valid @RequestBody UpdatePermissionRequest req) {
        AdminPermission p = rbacService.updatePermission(id, req.getName(), req.getModule());
        return Result.ok(toPermissionMap(p));
    }

    @DeleteMapping("/permissions/{id}")
    @RequirePermission("admin:permission:write")
    @AuditAction(value = "DELETE_PERMISSION", targetType = "admin_permission")
    public Result<Void> deletePermission(@PathVariable("id") Long id) {
        rbacService.deletePermission(id);
        return Result.ok();
    }

    @GetMapping("/roles")
    @RequirePermission("admin:role:read")
    public Result<List<RoleVO>> listRoles() {
        return Result.ok(rbacService.listRoles());
    }

    @PostMapping("/roles")
    @RequirePermission("admin:role:write")
    @AuditAction(value = "CREATE_ROLE", targetType = "admin_role")
    public Result<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest req) {
        return Result.ok(rbacService.createRole(req.getCode(), req.getName(), req.getRemark()));
    }

    @GetMapping("/roles/{id}")
    @RequirePermission("admin:role:read")
    public Result<RoleVO> getRole(@PathVariable("id") Long id) {
        return Result.ok(rbacService.getRoleWithPermissions(id));
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("admin:role:write")
    @AuditAction(value = "UPDATE_ROLE", targetType = "admin_role")
    public Result<RoleVO> updateRole(@PathVariable("id") Long id, @Valid @RequestBody UpdateRoleRequest req) {
        return Result.ok(rbacService.updateRole(id, req.getName(), req.getRemark()));
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("admin:role:write")
    @AuditAction(value = "DELETE_ROLE", targetType = "admin_role")
    public Result<Void> deleteRole(@PathVariable("id") Long id) {
        rbacService.deleteRole(id);
        return Result.ok();
    }

    @PutMapping("/roles/{id}/permissions")
    @RequirePermission("admin:permission:assign")
    @AuditAction(value = "ASSIGN_ROLE_PERMISSIONS", targetType = "admin_role")
    public Result<Void> assignPermissions(@PathVariable("id") Long id,
                                          @Valid @RequestBody AssignPermissionsRequest req) {
        rbacService.assignRolePermissions(id, req.getPermissionIds());
        return Result.ok();
    }

    private static Map<String, Object> toPermissionMap(AdminPermission p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("code", p.getCode());
        m.put("name", p.getName());
        m.put("module", p.getModule());
        return m;
    }
}
