package com.dormitory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.HygieneCheck;
import com.dormitory.mapper.HygieneCheckMapper;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 卫生检查管理（宿管/管理员录入，所有人可查）
 */
@Api(tags = "卫生检查")
@RestController
@RequestMapping("/hygiene")
@RequiredArgsConstructor
public class HygieneController {

    private final HygieneCheckMapper hygieneCheckMapper;

    @ApiOperation("分页卫生检查记录")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Map<String, Object>> page = hygieneCheckMapper.pageDetail(new Page<>(pageNum, pageSize));
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增检查记录")
    @PostMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> add(@RequestBody HygieneCheck check) {
        check.setId(null);
        check.setCheckerId(UserContext.getUserId());
        hygieneCheckMapper.insert(check);
        return Result.success();
    }

    @ApiOperation("修改检查记录")
    @PutMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> edit(@RequestBody HygieneCheck check) {
        hygieneCheckMapper.updateById(check);
        return Result.success();
    }

    @ApiOperation("删除检查记录")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        hygieneCheckMapper.deleteById(id);
        return Result.success();
    }
}
