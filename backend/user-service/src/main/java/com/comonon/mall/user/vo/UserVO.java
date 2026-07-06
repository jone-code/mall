package com.comonon.mall.user.vo;

import com.comonon.mall.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private String phone;

    public static UserVO from(User u) {
        return from(u, null);
    }

    public static UserVO from(User u, String phone) {
        return new UserVO(u.getId(), u.getNickname(), u.getAvatarUrl(), u.getStatus(), phone);
    }
}
