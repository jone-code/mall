package com.comonon.mall.admin.web;

import com.comonon.mall.admin.security.AuditAction;
import com.comonon.mall.admin.security.AdminContext;
import com.comonon.mall.admin.security.AuditLogAspect;
import com.comonon.mall.admin.service.AdminLoginService;
import com.comonon.mall.admin.service.AdminTokenService;
import com.comonon.mall.admin.service.CaptchaService;
import com.comonon.mall.admin.web.dto.PasswordLoginRequest;
import com.comonon.mall.admin.web.dto.RefreshRequest;
import com.comonon.mall.admin.web.dto.VerifyLoginRequest;
import com.comonon.mall.common.security.JwtUtil;
import com.comonon.mall.common.web.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final CaptchaService captchaService;
    private final AdminLoginService loginService;
    private final AdminTokenService tokenService;
    private final JwtUtil jwtUtil;

    @GetMapping("/captcha")
    public Result<CaptchaService.CaptchaResult> captcha() {
        return Result.ok(captchaService.generate());
    }

    @PostMapping("/login/password")
    public Result<AdminLoginService.PasswordStepResult> passwordLogin(
            @Valid @RequestBody PasswordLoginRequest req,
            HttpServletRequest request) {
        return Result.ok(loginService.passwordStep(
                req.getUsername(), req.getPassword(),
                req.getCaptchaId(), req.getCaptcha(),
                AuditLogAspect.clientIp(request),
                request.getHeader("User-Agent")));
    }

    @PostMapping("/login/verify")
    public Result<AdminLoginService.VerifyStepResult> verify(
            @Valid @RequestBody VerifyLoginRequest req,
            HttpServletRequest request) {
        return Result.ok(loginService.verifyStep(
                req.getChallengeToken(), req.getSmsCode(),
                AuditLogAspect.clientIp(request),
                request.getHeader("User-Agent")));
    }

    @PostMapping("/token/refresh")
    public Result<AdminTokenService.IssuedTokens> refresh(@Valid @RequestBody RefreshRequest req,
                                                          HttpServletRequest request) {
        return Result.ok(tokenService.refresh(req.getAccessToken(), req.getRefreshToken(),
                AuditLogAspect.clientIp(request), request.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    @AuditAction(value = "LOGOUT")
    public Result<Void> logout(HttpServletRequest request) {
        AdminContext.Holder ctx = AdminContext.get();
        if (ctx == null) return Result.ok();
        // 估算 jti 剩余时间
        String auth = request.getHeader("Authorization");
        long remaining = 0;
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims c = jwtUtil.parse(auth.substring(7));
                if (c.getExpiration() != null) {
                    remaining = Math.max(0, (c.getExpiration().getTime() - System.currentTimeMillis()) / 1000L);
                }
            } catch (Exception ignore) {}
        }
        tokenService.logout(ctx.adminUserId(), ctx.sid(), ctx.jti(), remaining);
        return Result.ok();
    }
}
