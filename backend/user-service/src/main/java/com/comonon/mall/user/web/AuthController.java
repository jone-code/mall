package com.comonon.mall.user.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.common.constant.RedisKeys;
import com.comonon.mall.common.security.UserContext;
import com.comonon.mall.user.dto.ChangePhoneRequest;
import com.comonon.mall.user.dto.MockAvatarRequest;
import com.comonon.mall.user.dto.RefreshTokenRequest;
import com.comonon.mall.user.dto.SmsLoginRequest;
import com.comonon.mall.user.dto.SmsSendRequest;
import com.comonon.mall.user.dto.UpdateProfileRequest;
import com.comonon.mall.user.dto.WechatLoginRequest;
import com.comonon.mall.user.entity.User;
import com.comonon.mall.user.service.SmsCodeService;
import com.comonon.mall.user.service.TokenService;
import com.comonon.mall.user.service.UserService;
import com.comonon.mall.user.vo.LoginVO;
import com.comonon.mall.user.vo.SessionVO;
import com.comonon.mall.user.vo.TokenVO;
import com.comonon.mall.user.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SmsCodeService smsCodeService;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSms(@Valid @RequestBody SmsSendRequest req,
                                               HttpServletRequest httpReq) {
        String scene = req.getScene() == null ? RedisKeys.SCENE_LOGIN : req.getScene();
        String ip = clientIp(httpReq);
        smsCodeService.send(req.getPhone(), ip, scene);
        return Result.ok(Map.of("ttl", 300));
    }

    @PostMapping("/login/sms")
    public Result<LoginVO> loginBySms(@Valid @RequestBody SmsLoginRequest req) {
        smsCodeService.verify(req.getPhone(), req.getCode(), RedisKeys.SCENE_LOGIN);
        UserService.LoginUserResult r = userService.loginOrRegisterByPhone(req.getPhone());
        TokenService.TokenPair pair = tokenService.issue(r.getUser().getId(),
                req.getDeviceId(), req.getDeviceType());
        UserVO userVo = userService.toUserVO(r.getUser());
        if (userVo.getPhone() == null) {
            userVo.setPhone(req.getPhone());
        }
        return Result.ok(LoginVO.of(pair, userVo, r.isNewUser()));
    }

    @PostMapping("/oauth/wechat")
    public Result<LoginVO> loginByWechat(@Valid @RequestBody WechatLoginRequest req) {
        UserService.LoginUserResult r = userService.loginOrRegisterByWechat(req.getCode());
        TokenService.TokenPair pair = tokenService.issue(r.getUser().getId(),
                req.getDeviceId(), req.getDeviceType());
        return Result.ok(LoginVO.of(pair, userService.toUserVO(r.getUser()), r.isNewUser()));
    }

    @PostMapping("/token/refresh")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        TokenService.TokenPair pair = tokenService.refresh(req.getSid(), req.getRefreshToken());
        return Result.ok(TokenVO.of(pair));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        var p = UserContext.get();
        if (p != null) {
            Map<Object, Object> session = tokenService.getSession(p.getSid());
            long exp = 0;
            try {
                Object e = session.get(TokenService.F_ACCESS_EXP);
                if (e != null) exp = Long.parseLong(e.toString());
            } catch (NumberFormatException ignored) {}
            tokenService.logout(p.getUserId(), p.getSid(), p.getJti(), exp);
        }
        return Result.ok();
    }

    @PostMapping("/logout/all")
    public Result<Void> logoutAll() {
        var p = UserContext.get();
        if (p != null) {
            tokenService.logoutAll(p.getUserId());
        }
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        var p = UserContext.get();
        User u = userService.getById(p.getUserId());
        return Result.ok(userService.toUserVO(u));
    }

    @PutMapping("/me")
    public Result<UserVO> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        var p = UserContext.get();
        User u = userService.updateProfile(p.getUserId(), req.getNickname(), req.getAvatarUrl());
        return Result.ok(userService.toUserVO(u));
    }

    @PutMapping("/me/phone")
    public Result<UserVO> changePhone(@Valid @RequestBody ChangePhoneRequest req) {
        var p = UserContext.get();
        User u = userService.changePhone(p.getUserId(), req.getPhone(), req.getCode());
        return Result.ok(userService.toUserVO(u));
    }

    @PostMapping("/me/avatar")
    public Result<Map<String, String>> mockAvatar(@RequestBody(required = false) MockAvatarRequest req) {
        var p = UserContext.get();
        String local = req != null ? req.getLocalPath() : null;
        String url = userService.mockAvatarUrl(p.getUserId(), local);
        userService.updateProfile(p.getUserId(), null, url);
        return Result.ok(Map.of("avatarUrl", url));
    }

    @GetMapping("/me/sessions")
    public Result<List<SessionVO>> mySessions() {
        var p = UserContext.get();
        Set<String> sids = tokenService.listSessionIds(p.getUserId());
        List<SessionVO> result = new ArrayList<>();
        if (sids != null) {
            for (String sid : sids) {
                Map<Object, Object> s = tokenService.getSession(sid);
                if (s == null || s.isEmpty()) continue;
                result.add(new SessionVO(
                        sid,
                        (String) s.get(TokenService.F_DEVICE_ID),
                        (String) s.get(TokenService.F_DEVICE_TYPE),
                        parseLong(s.get(TokenService.F_CREATED_AT)),
                        parseLong(s.get(TokenService.F_LAST_ACTIVE_AT)),
                        sid.equals(p.getSid())
                ));
            }
        }
        return Result.ok(result);
    }

    @DeleteMapping("/me/sessions/{sid}")
    public Result<Void> killSession(@PathVariable("sid") String sid) {
        var p = UserContext.get();
        tokenService.kickSession(p.getUserId(), sid);
        return Result.ok();
    }

    private Long parseLong(Object o) {
        if (o == null) return null;
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return comma >= 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}
