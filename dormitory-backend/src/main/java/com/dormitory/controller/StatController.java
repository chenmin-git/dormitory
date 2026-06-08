package com.dormitory.controller;

import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.service.StatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据看板统计
 */
@Api(tags = "数据看板")
@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
@PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
public class StatController {

    private final StatService statService;

    @ApiOperation("概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statService.overview());
    }
}
