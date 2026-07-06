package com.comonon.mall.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemberDetailVO {
    private Long id;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private List<MemberAddressVO> addresses = new ArrayList<>();
    private List<MemberOrderBriefVO> recentOrders = new ArrayList<>();
}
