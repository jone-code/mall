package com.comonon.mall.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.comonon.mall.admin.entity.AdminPermission;
import com.comonon.mall.admin.entity.AdminRole;
import com.comonon.mall.admin.entity.AdminRolePermission;
import com.comonon.mall.admin.entity.AdminUserRole;
import com.comonon.mall.admin.mapper.AdminPermissionMapper;
import com.comonon.mall.admin.mapper.AdminRoleMapper;
import com.comonon.mall.admin.mapper.AdminRolePermissionMapper;
import com.comonon.mall.admin.mapper.AdminUserRoleMapper;
import com.comonon.mall.admin.vo.RoleVO;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminRbacService {

    private static final String SUPER_ADMIN_CODE = "SUPER_ADMIN";

    private final AdminRoleMapper roleMapper;
    private final AdminPermissionMapper permissionMapper;
    private final AdminRolePermissionMapper rolePermissionMapper;
    private final AdminUserRoleMapper userRoleMapper;
    private final PermissionService permissionService;

    public List<AdminPermission> listPermissions() {
        return permissionMapper.selectList(
                Wrappers.<AdminPermission>lambdaQuery().orderByAsc(AdminPermission::getModule, AdminPermission::getCode));
    }

    public List<RoleVO> listRoles() {
        List<AdminRole> roles = roleMapper.selectList(
                Wrappers.<AdminRole>lambdaQuery().orderByAsc(AdminRole::getId));
        Map<Long, Long> countByRole = countPermissionsByRole();
        return roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            vo.setId(r.getId());
            vo.setCode(r.getCode());
            vo.setName(r.getName());
            vo.setRemark(r.getRemark());
            vo.setPermissionCount(countByRole.getOrDefault(r.getId(), 0L).intValue());
            return vo;
        }).toList();
    }

    public RoleVO getRoleWithPermissions(Long roleId) {
        AdminRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "角色不存在");
        }
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setCode(role.getCode());
        vo.setName(role.getName());
        vo.setRemark(role.getRemark());
        List<Long> permIds = rolePermissionMapper.selectList(
                        Wrappers.<AdminRolePermission>lambdaQuery().eq(AdminRolePermission::getRoleId, roleId))
                .stream().map(AdminRolePermission::getPermissionId).toList();
        vo.setPermissionIds(permIds);
        vo.setPermissionCount(permIds.size());
        return vo;
    }

    @Transactional
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        AdminRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "角色不存在");
        }
        rolePermissionMapper.delete(
                Wrappers.<AdminRolePermission>lambdaQuery().eq(AdminRolePermission::getRoleId, roleId));
        for (Long permId : permissionIds == null ? List.<Long>of() : permissionIds) {
            AdminRolePermission rp = new AdminRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
        bumpUsersByRole(roleId);
    }

    @Transactional
    public RoleVO createRole(String code, String name, String remark) {
        String normalizedCode = code.trim().toUpperCase();
        if (roleMapper.selectOne(Wrappers.<AdminRole>lambdaQuery().eq(AdminRole::getCode, normalizedCode)) != null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "角色编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        AdminRole role = new AdminRole();
        role.setCode(normalizedCode);
        role.setName(name.trim());
        role.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        roleMapper.insert(role);
        return getRoleWithPermissions(role.getId());
    }

    @Transactional
    public RoleVO updateRole(Long roleId, String name, String remark) {
        AdminRole role = requireRole(roleId);
        role.setName(name.trim());
        role.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        return getRoleWithPermissions(roleId);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        AdminRole role = requireRole(roleId);
        if (SUPER_ADMIN_CODE.equals(role.getCode())) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "内置超级管理员角色不可删除");
        }
        long userCount = userRoleMapper.selectCount(
                Wrappers.<AdminUserRole>lambdaQuery().eq(AdminUserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "角色仍有关联管理员，请先调整账号角色");
        }
        rolePermissionMapper.delete(
                Wrappers.<AdminRolePermission>lambdaQuery().eq(AdminRolePermission::getRoleId, roleId));
        roleMapper.deleteById(roleId);
    }

    @Transactional
    public AdminPermission createPermission(String code, String name, String module) {
        String normalizedCode = code.trim().toLowerCase();
        if (permissionMapper.selectOne(Wrappers.<AdminPermission>lambdaQuery().eq(AdminPermission::getCode, normalizedCode)) != null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "权限编码已存在");
        }
        AdminPermission permission = new AdminPermission();
        permission.setCode(normalizedCode);
        permission.setName(name.trim());
        permission.setModule(StringUtils.hasText(module) ? module.trim() : null);
        permissionMapper.insert(permission);
        return permission;
    }

    @Transactional
    public AdminPermission updatePermission(Long permissionId, String name, String module) {
        AdminPermission permission = requirePermission(permissionId);
        permission.setName(name.trim());
        permission.setModule(StringUtils.hasText(module) ? module.trim() : null);
        permissionMapper.updateById(permission);
        return permission;
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        requirePermission(permissionId);
        List<Long> roleIds = rolePermissionMapper.selectList(
                        Wrappers.<AdminRolePermission>lambdaQuery().eq(AdminRolePermission::getPermissionId, permissionId))
                .stream().map(AdminRolePermission::getRoleId).distinct().toList();
        rolePermissionMapper.delete(
                Wrappers.<AdminRolePermission>lambdaQuery().eq(AdminRolePermission::getPermissionId, permissionId));
        permissionMapper.deleteById(permissionId);
        for (Long roleId : roleIds) {
            bumpUsersByRole(roleId);
        }
    }

    private AdminRole requireRole(Long roleId) {
        AdminRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "角色不存在");
        }
        return role;
    }

    private AdminPermission requirePermission(Long permissionId) {
        AdminPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "权限不存在");
        }
        return permission;
    }

    private Map<Long, Long> countPermissionsByRole() {
        List<AdminRolePermission> rows = rolePermissionMapper.selectList(null);
        Map<Long, Long> map = new HashMap<>();
        for (AdminRolePermission rp : rows) {
            map.merge(rp.getRoleId(), 1L, Long::sum);
        }
        return map;
    }

    private void bumpUsersByRole(Long roleId) {
        List<Long> adminUserIds = userRoleMapper.selectAdminUserIdsByRole(roleId);
        for (Long adminUserId : adminUserIds) {
            permissionService.bumpVersion(adminUserId);
        }
    }
}
