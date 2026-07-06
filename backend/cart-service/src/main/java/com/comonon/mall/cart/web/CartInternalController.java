package com.comonon.mall.cart.web;

import com.comonon.mall.cart.dto.RemoveItemsRequest;
import com.comonon.mall.cart.service.CartService;
import com.comonon.mall.cart.vo.InternalCartLineVO;
import com.comonon.mall.common.web.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部购物车接口，供 order-service 调用。不经 X-User-Id 拦截（userId 走路径）。
 */
@RestController
@RequestMapping("/internal/cart")
@RequiredArgsConstructor
public class CartInternalController {

    private final CartService cartService;

    @GetMapping("/{userId}/checkout")
    public Result<List<InternalCartLineVO>> selected(@PathVariable Long userId) {
        return Result.ok(cartService.selectedLines(userId));
    }

    @PostMapping("/{userId}/remove")
    public Result<Void> remove(@PathVariable Long userId, @Valid @RequestBody RemoveItemsRequest req) {
        cartService.removeLines(userId, req.getSkuIds());
        return Result.ok();
    }
}
