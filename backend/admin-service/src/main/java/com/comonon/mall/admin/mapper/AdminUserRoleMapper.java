package com.comonon.mall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.admin.entity.AdminUserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AdminUserRoleMapper extends BaseMapper<AdminUserRole> {
    @Select("SELECT r.code FROM admin_role r INNER JOIN admin_user_role ur ON ur.role_id = r.id WHERE ur.admin_user_id = #{adminUserId}")
    List<String> selectRoleCodes(@Param("adminUserId") Long adminUserId);

    @Select("SELECT admin_user_id FROM admin_user_role WHERE role_id = #{roleId}")
    List<Long> selectAdminUserIdsByRole(@Param("roleId") Long roleId);
}
