package com.comonon.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.user.entity.User;
import com.comonon.mall.user.entity.UserIdentity;
import com.comonon.mall.user.mapper.UserIdentityMapper;
import com.comonon.mall.user.mapper.UserMapper;
import com.comonon.mall.user.vo.UserBriefVO;
import com.comonon.mall.user.vo.UserVO;
import com.comonon.mall.user.oauth.WechatOAuthClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录/注册合一服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserIdentityMapper identityMapper;
    private final WechatOAuthClient wechatOAuthClient;

    /**
     * 手机号登录/注册：若 user_identity 不存在则自动创建 user + identity。
     */
    @Transactional
    public LoginUserResult loginOrRegisterByPhone(String phone) {
        UserIdentity identity = identityMapper.selectOne(new QueryWrapper<UserIdentity>()
                .eq("identity_type", UserIdentity.TYPE_PHONE)
                .eq("identifier", phone));
        boolean isNew = false;
        User user;
        if (identity == null) {
            user = createUser();
            identity = createIdentity(user.getId(), UserIdentity.TYPE_PHONE, phone, null);
            isNew = true;
        } else {
            user = userMapper.selectById(identity.getUserId());
            if (user == null) {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "用户数据缺失");
            }
            checkUser(user);
        }
        return new LoginUserResult(user, isNew);
    }

    @Transactional
    public LoginUserResult loginOrRegisterByWechat(String code) {
        WechatOAuthClient.WechatUser wx = wechatOAuthClient.codeToOpenid(code);
        if (wx == null || wx.openid() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "微信换 openid 失败");
        }
        UserIdentity identity = identityMapper.selectOne(new QueryWrapper<UserIdentity>()
                .eq("identity_type", UserIdentity.TYPE_WECHAT)
                .eq("identifier", wx.openid()));
        boolean isNew = false;
        User user;
        if (identity == null) {
            user = createUser();
            identity = createIdentity(user.getId(), UserIdentity.TYPE_WECHAT, wx.openid(), wx.unionid());
            isNew = true;
        } else {
            user = userMapper.selectById(identity.getUserId());
            if (user == null) {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "用户数据缺失");
            }
            checkUser(user);
        }
        return new LoginUserResult(user, isNew);
    }

    public User getById(long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.BAD_REQUEST, "用户不存在");
        return u;
    }

    public List<UserBriefVO> listBriefByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(ids).stream().map(u -> {
            UserBriefVO vo = new UserBriefVO();
            vo.setId(u.getId());
            vo.setNickname(u.getNickname());
            return vo;
        }).toList();
    }

    public User updateProfile(long userId, String nickname, String avatarUrl) {
        User u = getById(userId);
        if (nickname != null && !nickname.isBlank()) {
            u.setNickname(nickname.trim());
        }
        if (avatarUrl != null) {
            u.setAvatarUrl(avatarUrl.isBlank() ? null : avatarUrl.trim());
        }
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return u;
    }

    /** 查询用户绑定的手机号（若有）。 */
    public String findPhoneByUserId(long userId) {
        UserIdentity identity = identityMapper.selectOne(new QueryWrapper<UserIdentity>()
                .eq("user_id", userId)
                .eq("identity_type", UserIdentity.TYPE_PHONE)
                .last("LIMIT 1"));
        return identity == null ? null : identity.getIdentifier();
    }

    public UserVO toUserVO(User u) {
        return UserVO.from(u, findPhoneByUserId(u.getId()));
    }

    /** Mock 换绑手机：验证码固定 888888。 */
    @Transactional
    public User changePhone(long userId, String newPhone, String code) {
        if (!"888888".equals(code)) {
            throw BizException.of(ErrorCode.SMS_CODE_INVALID, "验证码错误（Mock: 888888）");
        }
        UserIdentity existing = identityMapper.selectOne(new QueryWrapper<UserIdentity>()
                .eq("identity_type", UserIdentity.TYPE_PHONE)
                .eq("identifier", newPhone));
        if (existing != null && !existing.getUserId().equals(userId)) {
            throw BizException.of(ErrorCode.BAD_REQUEST, "该手机号已被其他账号绑定");
        }
        UserIdentity mine = identityMapper.selectOne(new QueryWrapper<UserIdentity>()
                .eq("user_id", userId)
                .eq("identity_type", UserIdentity.TYPE_PHONE)
                .last("LIMIT 1"));
        if (mine == null) {
            createIdentity(userId, UserIdentity.TYPE_PHONE, newPhone, null);
        } else {
            mine.setIdentifier(newPhone);
            identityMapper.updateById(mine);
        }
        return getById(userId);
    }

    /** Mock 头像 URL（picsum 占位，后续接 OSS）。 */
    public String mockAvatarUrl(long userId, String localPath) {
        int seed = localPath != null ? localPath.hashCode() : (int) userId;
        return "https://picsum.photos/seed/u" + userId + "a" + Math.abs(seed) + "/200/200";
    }

    private User createUser() {
        User u = new User();
        u.setNickname("temp"); // 占位，写库后用 id 生成默认昵称回填
        u.setStatus(0);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);
        String defaultNickname = "用户_" + defaultNicknameSuffix(u.getId());
        u.setNickname(defaultNickname);
        userMapper.updateById(u);
        return u;
    }

    private UserIdentity createIdentity(long userId, String type, String identifier, String unionId) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setIdentityType(type);
        identity.setIdentifier(identifier);
        identity.setUnionId(unionId);
        identity.setVerified(1);
        identity.setCreatedAt(LocalDateTime.now());
        identityMapper.insert(identity);
        return identity;
    }

    /**
     * 取 userId 后 6 位 hex（不足前补 0），作为默认昵称后缀。
     */
    static String defaultNicknameSuffix(long userId) {
        String hex = Long.toHexString(userId);
        if (hex.length() >= 6) return hex.substring(hex.length() - 6);
        return ("000000" + hex).substring(hex.length()); // 补到 6 位
    }

    private void checkUser(User user) {
        if (user.getStatus() == null) return;
        if (user.getStatus() == 1) throw new BizException(ErrorCode.ACCOUNT_DISABLED, "账号已禁用");
        if (user.getStatus() == 2) throw new BizException(ErrorCode.ACCOUNT_DEACTIVATED, "账号已注销");
    }

    @Data
    public static class LoginUserResult {
        private final User user;
        private final boolean newUser;
    }
}
