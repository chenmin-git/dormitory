package com.dormitory.controller;

import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.SysUser;
import com.dormitory.service.SysUserService;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理（仅管理员）
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuth(RoleConstant.ADMIN)
public class SysUserController {

    private final SysUserService sysUserService;

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(PageQuery query,
                                            @RequestParam(required = false) String role) {
        return Result.success(sysUserService.page(query, role));
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        sysUserService.add(user);
        return Result.success();
    }

    @ApiOperation("修改用户")
    @PutMapping
    public Result<Void> edit(@RequestBody SysUser user) {
        sysUserService.edit(user);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    @ApiOperation("重置密码为123456")
    @PutMapping("/reset/{id}")
    public Result<Void> reset(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    @ApiOperation("修改自己的密码")
    @PutMapping("/password")
    @PreAuth // 仅需登录，覆盖类上的 ADMIN 限制
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        sysUserService.changePassword(UserContext.getUserId(),
                body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    @ApiOperation("宿管列表(派单下拉用)")
    @GetMapping("/managers")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<java.util.List<SysUser>> managers() {
        java.util.List<SysUser> list = sysUserService.lambdaQuery()
                .eq(SysUser::getRole, RoleConstant.MANAGER)
                .list();
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }
}
