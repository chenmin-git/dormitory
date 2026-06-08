package com.dormitory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Announcement;
import com.dormitory.mapper.AnnouncementMapper;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公告管理（所有人可看，管理员/宿管可发布）
 */
@Api(tags = "公告管理")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper announcementMapper;

    @ApiOperation("分页公告列表")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Map<String, Object>> page = announcementMapper.pageDetail(new Page<>(pageNum, pageSize));
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("发布公告")
    @PostMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> add(@RequestBody Announcement announcement) {
        announcement.setId(null);
        announcement.setPublisherId(UserContext.getUserId());
        announcementMapper.insert(announcement);
        return Result.success();
    }

    @ApiOperation("修改公告")
    @PutMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> edit(@RequestBody Announcement announcement) {
        announcementMapper.updateById(announcement);
        return Result.success();
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}
