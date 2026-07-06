package com.comonon.mall.admin.service;

import com.comonon.mall.admin.client.OrderQueryClient;
import com.comonon.mall.admin.entity.MemberUser;
import com.comonon.mall.admin.mapper.MemberUserMapper;
import com.comonon.mall.admin.vo.MemberDetailVO;
import com.comonon.mall.admin.vo.MemberUserVO;
import com.comonon.mall.common.web.BusinessException;
import com.comonon.mall.common.web.ErrorCodes;
import com.comonon.mall.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberUserService {

    private final MemberUserMapper memberUserMapper;
    private final OrderQueryClient orderQueryClient;

    public PageResult<MemberUserVO> list(String keyword, Integer status, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        List<MemberUserVO> list = memberUserMapper.selectMemberPage(keyword, status, offset, s);
        long total = memberUserMapper.countMembers(keyword, status);
        return PageResult.of(list, p, s, total);
    }

    public MemberDetailVO detail(Long id) {
        MemberUserVO base = memberUserMapper.selectMemberById(id);
        if (base == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "用户不存在");
        }
        MemberDetailVO vo = new MemberDetailVO();
        vo.setId(base.getId());
        vo.setNickname(base.getNickname());
        vo.setPhone(base.getPhone());
        vo.setAvatarUrl(base.getAvatarUrl());
        vo.setStatus(base.getStatus());
        vo.setCreatedAt(base.getCreatedAt());
        vo.setAddresses(memberUserMapper.selectAddressesByUserId(id));
        vo.setRecentOrders(orderQueryClient.recentOrders(id, 10));
        return vo;
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        if (status < 0 || status > 2) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "status 必须为 0/1/2");
        }
        MemberUser u = memberUserMapper.selectById(id);
        if (u == null) {
            throw new BusinessException(ErrorCodes.INVALID_CREDENTIALS, "用户不存在");
        }
        u.setStatus(status);
        u.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(u);
    }

    public Map<String, Long> stats() {
        long todayNew = memberUserMapper.countCreatedSince(LocalDate.now().atStartOfDay());
        return Map.of("todayNewUsers", todayNew);
    }
}
