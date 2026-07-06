package com.comonon.mall.user.web;

import com.comonon.mall.common.web.Result;
import com.comonon.mall.user.service.UserService;
import com.comonon.mall.user.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部用户接口，供 order-service 等拉取用户展示信息。
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/brief")
    public Result<List<UserBriefVO>> brief(@RequestParam("ids") List<Long> ids) {
        return Result.ok(userService.listBriefByIds(ids));
    }
}
