package com.dormitory.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Bed;
import com.dormitory.mapper.BedMapper;
import com.dormitory.service.AllocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 床位管理 + 入住分配
 */
@Api(tags = "床位管理")
@RestController
@RequestMapping("/bed")
@RequiredArgsConstructor
public class BedController {

    private final BedMapper bedMapper;
    private final AllocationService allocationService;

    @ApiOperation("某房间的床位列表(含入住学生)")
    @GetMapping("/listByRoom/{roomId}")
    public Result<List<Map<String, Object>>> listByRoom(@PathVariable Long roomId) {
        return Result.success(bedMapper.listByRoom(roomId));
    }

    @ApiOperation("查询空闲床位(分配下拉用)")
    @GetMapping("/free")
    public Result<List<Bed>> freeBeds(@RequestParam(required = false) Long roomId) {
        return Result.success(bedMapper.selectList(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getStatus, 0)
                .eq(roomId != null, Bed::getRoomId, roomId)));
    }

    @ApiOperation("新增床位")
    @PostMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> add(@RequestBody Bed bed) {
        bed.setId(null);
        bed.setStudentId(null);
        bed.setStatus(0);
        bedMapper.insert(bed);
        return Result.success();
    }

    @ApiOperation("删除床位")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        Bed bed = bedMapper.selectById(id);
        if (bed != null && bed.getStudentId() != null) {
            throw new BusinessException("床位已有学生入住，无法删除");
        }
        bedMapper.deleteById(id);
        return Result.success();
    }

    @ApiOperation("分配学生入住床位")
    @PostMapping("/allocate")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> allocate(@RequestBody Map<String, Long> body) {
        allocationService.allocate(body.get("studentId"), body.get("bedId"));
        return Result.success();
    }

    @ApiOperation("学生退宿")
    @PostMapping("/deallocate/{studentId}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> deallocate(@PathVariable Long studentId) {
        allocationService.deallocate(studentId);
        return Result.success();
    }
}
