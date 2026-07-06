package com.comonon.mall.user.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.user.service.AddressService;
import com.comonon.mall.user.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部地址接口，供 order-service 取地址快照。不经 JWT 拦截（userId 显式传入并校验归属）。
 */
@RestController
@RequestMapping("/internal/addresses")
@RequiredArgsConstructor
public class InternalAddressController {

    private final AddressService addressService;

    @GetMapping("/{id}")
    public Result<AddressVO> get(@PathVariable Long id, @RequestParam Long userId) {
        return Result.ok(AddressVO.from(addressService.requireOwned(userId, id)));
    }
}
