package com.comonon.mall.admin.web;

import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.RequirePermission;
import com.comonon.mall.admin.service.MemberUserService;
import com.comonon.mall.admin.vo.MemberDetailVO;
import com.comonon.mall.admin.vo.MemberUserVO;
import com.comonon.mall.admin.web.dto.UpdateMemberStatusRequest;
import com.comonon.mall.common.web.PageResult;
import com.comonon.mall.common.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberUserService memberUserService;

    @GetMapping
    @RequirePermission("member:read")
    public Result<PageResult<MemberUserVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.ok(memberUserService.list(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    @RequirePermission("member:read")
    public Result<MemberDetailVO> detail(@PathVariable("id") Long id) {
        return Result.ok(memberUserService.detail(id));
    }

    @GetMapping("/stats")
    @RequirePermission("member:read")
    public Result<Map<String, Long>> stats() {
        return Result.ok(memberUserService.stats());
    }

    @PutMapping("/{id}/status")
    @RequirePermission("member:write")
    @AuditAction(value = "UPDATE_MEMBER_STATUS", targetType = "user")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @Valid @RequestBody UpdateMemberStatusRequest req) {
        memberUserService.updateStatus(id, req.getStatus());
        return Result.ok();
    }
}
