package com.comonon.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.comonon.mall.user.entity.User;
import com.comonon.mall.user.entity.UserIdentity;
import com.comonon.mall.user.mapper.UserIdentityMapper;
import com.comonon.mall.user.mapper.UserMapper;
import com.comonon.mall.user.oauth.WechatOAuthClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserMapper userMapper;
    @Mock UserIdentityMapper identityMapper;
    @Mock WechatOAuthClient wechatOAuthClient;
    @InjectMocks UserService userService;

    @Test
    void loginOrRegisterByPhone_autoCreateWhenNotExist() {
        when(identityMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(0xABCDEFL);
            return 1;
        }).when(userMapper).insert(any(User.class));

        UserService.LoginUserResult result = userService.loginOrRegisterByPhone("13800000000");

        assertTrue(result.isNewUser());
        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCap.capture());
        verify(userMapper).updateById(userCap.capture());
        // 默认昵称 用户_xxxxxx 取后6位 hex
        User updated = userCap.getAllValues().get(1);
        assertEquals("用户_abcdef", updated.getNickname());

        ArgumentCaptor<UserIdentity> idCap = ArgumentCaptor.forClass(UserIdentity.class);
        verify(identityMapper).insert(idCap.capture());
        assertEquals("PHONE", idCap.getValue().getIdentityType());
        assertEquals("13800000000", idCap.getValue().getIdentifier());
    }

    @Test
    void loginOrRegisterByPhone_existing() {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(10L);
        identity.setIdentityType("PHONE");
        identity.setIdentifier("13800000000");
        when(identityMapper.selectOne(any(Wrapper.class))).thenReturn(identity);
        User u = new User();
        u.setId(10L);
        u.setStatus(0);
        u.setNickname("nick");
        when(userMapper.selectById(10L)).thenReturn(u);

        UserService.LoginUserResult result = userService.loginOrRegisterByPhone("13800000000");

        assertFalse(result.isNewUser());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void loginOrRegisterByWechat_autoCreate() {
        when(wechatOAuthClient.codeToOpenid("c1")).thenReturn(
                new WechatOAuthClient.WechatUser("stub_openid_c1", null));
        when(identityMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        UserService.LoginUserResult r = userService.loginOrRegisterByWechat("c1");
        assertTrue(r.isNewUser());

        ArgumentCaptor<UserIdentity> idCap = ArgumentCaptor.forClass(UserIdentity.class);
        verify(identityMapper).insert(idCap.capture());
        assertEquals("WECHAT", idCap.getValue().getIdentityType());
        assertEquals("stub_openid_c1", idCap.getValue().getIdentifier());
    }

    @Test
    void defaultNicknameSuffix() {
        assertEquals("000001", UserService.defaultNicknameSuffix(1L));
        assertEquals("abcdef", UserService.defaultNicknameSuffix(0xABCDEFL));
        assertEquals("abcdef", UserService.defaultNicknameSuffix(0x12abcdefL));
    }
}
