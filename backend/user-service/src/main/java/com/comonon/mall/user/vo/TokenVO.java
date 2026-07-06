package com.comonon.mall.user.vo;

import com.comonon.mall.user.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenVO {
    private String accessToken;
    private String refreshToken;
    private long accessExp;

    public static TokenVO of(TokenService.TokenPair pair) {
        return new TokenVO(pair.getAccessToken(), pair.getRefreshToken(), pair.getAccessExp());
    }
}
