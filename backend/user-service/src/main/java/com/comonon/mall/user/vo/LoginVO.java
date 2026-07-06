package com.comonon.mall.user.vo;

import com.comonon.mall.user.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private String sid;
    private long accessExp;
    private UserVO user;
    private boolean isNewUser;

    public static LoginVO of(TokenService.TokenPair pair, UserVO user, boolean isNewUser) {
        return new LoginVO(pair.getAccessToken(), pair.getRefreshToken(),
                pair.getSid(), pair.getAccessExp(), user, isNewUser);
    }
}
