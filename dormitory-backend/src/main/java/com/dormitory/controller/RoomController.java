package com.dormitory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Room;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.RoomMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 房间管理
 */
@Api(tags = "房间管理")
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;

    @ApiOperation("分页房间列表")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long buildingId) {
        IPage<Map<String, Object>> page = roomMapper.pageWithBuilding(new Page<>(pageNum, pageSize), buildingId);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("按楼栋查房间(下拉用)")
    @GetMapping("/listByBuilding/{buildingId}")
    public Result<List<Room>> listByBuilding(@PathVariable Long buildingId) {
        return Result.success(roomMapper.selectList(
                Wrappers.<Room>lambdaQuery().eq(Room::getBuildingId, buildingId)));
    }

    @ApiOperation("新增房间(按容量自动生成床位)")
    @PostMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> add(@RequestBody Room room) {
        room.setId(null);
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        roomMapper.insert(room);
        // 按容量自动生成床位：1床、2床...N床
        int capacity = room.getCapacity() == null ? 0 : room.getCapacity();
        for (int i = 1; i <= capacity; i++) {
            Bed bed = new Bed();
            bed.setRoomId(room.getId());
            bed.setBedNo(i + "床");
            bed.setStatus(0);
            bedMapper.insert(bed);
        }
        return Result.success();
    }

    @ApiOperation("修改房间")
    @PutMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> edit(@RequestBody Room room) {
        roomMapper.updateById(room);
        return Result.success();
    }

    @ApiOperation("删除房间")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        roomMapper.deleteById(id);
        return Result.success();
    }
}
