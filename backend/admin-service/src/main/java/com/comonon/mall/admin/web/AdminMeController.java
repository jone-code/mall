package com.comonon.mall.admin.web;

import com.comonon.mall.admin.entity.AdminUser;
import com.comonon.mall.admin.security.AdminContext;
import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.service.AdminUserService;
import com.comonon.mall.admin.web.dto.ChangePasswordRequest;
import com.comonon.mall.common.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/me")
@RequiredArgsConstructor
public class AdminMeController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<Map<String, Object>> me() {
        AdminContext.Holder ctx = AdminContext.get();
        AdminUser u = adminUserService.findById(ctx.adminUserId());
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("username", u.getUsername());
        map.put("realName", u.getRealName());
        map.put("phone", u.getPhone());
        map.put("email", u.getEmail());
        map.put("status", u.getStatus());
        map.put("roles", ctx.roles());
        map.put("permissions", ctx.permissions());
        return Result.ok(map);
    }

    @PostMapping("/password")
    @AuditAction(value = "CHANGE_PASSWORD", targetType = "admin_user")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        AdminContext.Holder ctx = AdminContext.get();
        adminUserService.changePassword(ctx.adminUserId(), req.getOldPassword(), req.getNewPassword());
        return Result.ok();
    }
}
