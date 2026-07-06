package com.comonon.mall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comonon.mall.admin.entity.AdminPermission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AdminPermissionMapper extends BaseMapper<AdminPermission> {

    @Select({
            "<script>",
            "SELECT DISTINCT p.* FROM admin_permission p",
            "INNER JOIN admin_role_permission rp ON rp.permission_id = p.id",
            "INNER JOIN admin_user_role ur ON ur.role_id = rp.role_id",
            "WHERE ur.admin_user_id = #{adminUserId}",
            "</script>"
    })
    List<AdminPermission> selectByAdminUserId(@Param("adminUserId") Long adminUserId);
}
