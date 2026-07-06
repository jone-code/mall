package com.comonon.mall.admin.security;

import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @RequirePermission 切面：校验当前请求 admin 是否包含目标 permission code。
 */
@Aspect
@Component
public class PermissionAspect {

    @Pointcut("@annotation(rp)")
    public void requirePerm(RequirePermission rp) {}

    @Before("requirePerm(rp)")
    public void check(RequirePermission rp) {
        AdminContext.Holder ctx = AdminContext.get();
        if (ctx == null) {
            throw new BusinessException(ErrorCodes.ACCESS_INVALID, "未登录");
        }
        Set<String> perms = ctx.permissions();
        if (perms == null || !perms.contains(rp.value())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "缺少权限: " + rp.value());
        }
    }
}
