package com.dormitory.controller;

import com.dormitory.common.Result;
import com.dormitory.dto.LoginDTO;
import com.dormitory.entity.SysUser;
import com.dormitory.service.AuthService;
import com.dormitory.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证接口
 */
@Api(tags = "认证模块")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @ApiOperation("获取当前登录用户")
    @GetMapping("/info")
    public Result<SysUser> info() {
        return Result.success(authService.currentUser());
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，前端删除 token 即可
        return Result.success();
    }
}
