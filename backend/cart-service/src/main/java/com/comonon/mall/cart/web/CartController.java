package com.comonon.mall.cart.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.cart.dto.AddCartItemRequest;
import com.comonon.mall.cart.dto.SelectAllRequest;
import com.comonon.mall.cart.dto.UpdateCartItemRequest;
import com.comonon.mall.cart.security.CartUserContext;
import com.comonon.mall.cart.service.CartService;
import com.comonon.mall.cart.vo.AddCartResultVO;
import com.comonon.mall.cart.vo.CartListVO;
import com.comonon.mall.cart.vo.CheckoutPreviewVO;
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

import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<CartListVO> list() {
        return Result.ok(cartService.list(CartUserContext.getUserId()));
    }

    @PostMapping("/items")
    public Result<AddCartResultVO> add(@Valid @RequestBody AddCartItemRequest req) {
        return Result.ok(cartService.addItem(CartUserContext.getUserId(), req));
    }

    @PutMapping("/items/{skuId}")
    public Result<Void> update(@PathVariable Long skuId, @Valid @RequestBody UpdateCartItemRequest req) {
        cartService.updateItem(CartUserContext.getUserId(), skuId, req);
        return Result.ok();
    }

    @PutMapping("/select-all")
    public Result<Void> selectAll(@Valid @RequestBody SelectAllRequest req) {
        cartService.selectAll(CartUserContext.getUserId(), Boolean.TRUE.equals(req.getSelected()));
        return Result.ok();
    }

    @DeleteMapping("/items/{skuId}")
    public Result<Void> delete(@PathVariable Long skuId) {
        cartService.deleteItem(CartUserContext.getUserId(), skuId);
        return Result.ok();
    }

    @DeleteMapping("/invalid")
    public Result<Map<String, Integer>> deleteInvalid() {
        int n = cartService.deleteInvalid(CartUserContext.getUserId());
        return Result.ok(Map.of("removed", n));
    }

    @GetMapping("/checkout-preview")
    public Result<CheckoutPreviewVO> checkoutPreview() {
        return Result.ok(cartService.checkoutPreview(CartUserContext.getUserId()));
    }
}
