package com.dormitory.controller;

import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Building;
import com.dormitory.service.BuildingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 楼栋管理
 */
@Api(tags = "楼栋管理")
@RestController
@RequestMapping("/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @ApiOperation("楼栋列表(含宿管)")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.success(buildingService.listWithManager());
    }

    @ApiOperation("新增楼栋")
    @PostMapping
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> add(@RequestBody Building building) {
        building.setId(null);
        buildingService.save(building);
        return Result.success();
    }

    @ApiOperation("修改楼栋")
    @PutMapping
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> edit(@RequestBody Building building) {
        buildingService.updateById(building);
        return Result.success();
    }

    @ApiOperation("删除楼栋")
    @DeleteMapping("/{id}")
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.removeById(id);
        return Result.success();
    }
}
