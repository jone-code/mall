package com.comonon.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.comonon.mall.common.api.ErrorCode;
import com.comonon.mall.common.exception.BizException;
import com.comonon.mall.user.dto.CreateAddressRequest;
import com.comonon.mall.user.dto.UpdateAddressRequest;
import com.comonon.mall.user.entity.User;
import com.comonon.mall.user.entity.UserAddress;
import com.comonon.mall.user.mapper.UserAddressMapper;
import com.comonon.mall.user.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final int MAX_ADDRESSES = 20;
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private final UserAddressMapper addressMapper;
    private final UserService userService;

    public List<AddressVO> list(Long userId) {
        ensureUserActive(userId);
        List<UserAddress> rows = addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdatedAt));
        return rows.stream().map(AddressVO::from).toList();
    }

    @Transactional
    public Long create(Long userId, CreateAddressRequest req) {
        ensureUserActive(userId);
        validatePhone(req.getPhone());
        long count = addressMapper.selectCount(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
        if (count >= MAX_ADDRESSES) {
            throw BizException.of(ErrorCode.ADDRESS_LIMIT_EXCEEDED, "地址数量已达上限");
        }
        boolean first = count == 0;
        boolean asDefault = first || Boolean.TRUE.equals(req.getIsDefault());
        if (asDefault) {
            clearDefault(userId);
        }
        UserAddress addr = new UserAddress();
        fill(addr, req.getReceiver(), req.getPhone(), req.getProvince(), req.getCity(),
                req.getDistrict(), req.getDetail(), req.getRegionCode());
        addr.setUserId(userId);
        addr.setIsDefault(asDefault ? 1 : 0);
        LocalDateTime now = LocalDateTime.now();
        addr.setCreatedAt(now);
        addr.setUpdatedAt(now);
        addressMapper.insert(addr);
        return addr.getId();
    }

    @Transactional
    public void update(Long userId, Long id, UpdateAddressRequest req) {
        ensureUserActive(userId);
        UserAddress addr = requireOwned(userId, id);
        if (req.getPhone() != null) {
            validatePhone(req.getPhone());
            addr.setPhone(req.getPhone().trim());
        }
        if (req.getReceiver() != null) {
            addr.setReceiver(req.getReceiver().trim());
        }
        if (req.getProvince() != null) {
            addr.setProvince(req.getProvince().trim());
        }
        if (req.getCity() != null) {
            addr.setCity(req.getCity().trim());
        }
        if (req.getDistrict() != null) {
            addr.setDistrict(req.getDistrict().trim());
        }
        if (req.getDetail() != null) {
            addr.setDetail(req.getDetail().trim());
        }
        if (req.getRegionCode() != null) {
            addr.setRegionCode(req.getRegionCode());
        }
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            clearDefault(userId);
            addr.setIsDefault(1);
        }
        addr.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(addr);
    }

    @Transactional
    public void setDefault(Long userId, Long id) {
        ensureUserActive(userId);
        requireOwned(userId, id);
        clearDefault(userId);
        UserAddress patch = new UserAddress();
        patch.setId(id);
        patch.setIsDefault(1);
        patch.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(patch);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ensureUserActive(userId);
        UserAddress addr = requireOwned(userId, id);
        boolean wasDefault = addr.getIsDefault() != null && addr.getIsDefault() == 1;
        addressMapper.deleteById(id);
        if (wasDefault) {
            List<UserAddress> rest = addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByAsc(UserAddress::getId));
            if (!rest.isEmpty()) {
                UserAddress next = rest.stream().min(Comparator.comparing(UserAddress::getId)).orElse(rest.get(0));
                next.setIsDefault(1);
                next.setUpdatedAt(LocalDateTime.now());
                addressMapper.updateById(next);
            }
        }
    }

    public UserAddress requireOwned(Long userId, Long id) {
        UserAddress addr = addressMapper.selectById(id);
        if (addr == null || !userId.equals(addr.getUserId())) {
            throw BizException.of(ErrorCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
        return addr;
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0)
                .set(UserAddress::getUpdatedAt, LocalDateTime.now()));
    }

    private void fill(UserAddress addr, String receiver, String phone, String province,
                      String city, String district, String detail, String regionCode) {
        addr.setReceiver(receiver.trim());
        addr.setPhone(phone.trim());
        addr.setProvince(province.trim());
        addr.setCity(city.trim());
        addr.setDistrict(district.trim());
        addr.setDetail(detail.trim());
        addr.setRegionCode(regionCode);
    }

    private void validatePhone(String phone) {
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) {
            throw BizException.badRequest("手机号格式不正确");
        }
    }

    private void ensureUserActive(Long userId) {
        User u = userService.getById(userId);
        if (u.getStatus() != null && u.getStatus() == 1) {
            throw BizException.of(ErrorCode.ACCOUNT_DISABLED, "账号已禁用");
        }
        if (u.getStatus() != null && u.getStatus() == 2) {
            throw BizException.of(ErrorCode.ACCOUNT_DEACTIVATED, "账号已注销");
        }
    }
}
