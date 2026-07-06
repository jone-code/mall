package com.comonon.mall.user.web;

import com.comonon.mall.common.api.Result;
import com.comonon.mall.common.security.UserContext;
import com.comonon.mall.user.dto.CreateAddressRequest;
import com.comonon.mall.user.dto.UpdateAddressRequest;
import com.comonon.mall.user.service.AddressService;
import com.comonon.mall.user.vo.AddressVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<AddressVO>> list() {
        Long userId = UserContext.get().getUserId();
        return Result.ok(addressService.list(userId));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody CreateAddressRequest req) {
        Long userId = UserContext.get().getUserId();
        Long id = addressService.create(userId, req);
        return Result.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateAddressRequest req) {
        Long userId = UserContext.get().getUserId();
        addressService.update(userId, id, req);
        return Result.ok();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = UserContext.get().getUserId();
        addressService.setDefault(userId, id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.get().getUserId();
        addressService.delete(userId, id);
        return Result.ok();
    }
}
