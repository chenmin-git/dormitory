package com.dormitory.controller;

import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Repair;
import com.dormitory.entity.Student;
import com.dormitory.service.RepairService;
import com.dormitory.service.StudentService;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 报修管理
 */
@Api(tags = "报修管理")
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final StudentService studentService;

    @ApiOperation("学生提交报修(AI自动分类)")
    @PostMapping
    @PreAuth(RoleConstant.STUDENT)
    public Result<Void> submit(@RequestBody Repair repair) {
        repairService.submit(UserContext.getUserId(), repair);
        return Result.success();
    }

    @ApiOperation("分页查询报修(学生仅看自己)")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long studentFilter = null;
        if (RoleConstant.STUDENT.equals(UserContext.getRole())) {
            Student student = studentService.getByUserId(UserContext.getUserId());
            // 没有学生档案则查不到数据
            studentFilter = student != null ? student.getId() : -1L;
        }
        return Result.success(repairService.page(pageNum, pageSize, status, studentFilter));
    }

    @ApiOperation("派单")
    @PutMapping("/assign")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> assign(@RequestBody Map<String, Long> body) {
        repairService.assign(body.get("repairId"), body.get("handlerId"));
        return Result.success();
    }

    @ApiOperation("完成维修")
    @PutMapping("/finish")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> finish(@RequestBody Map<String, Object> body) {
        Long repairId = Long.valueOf(body.get("repairId").toString());
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        repairService.finish(repairId, remark);
        return Result.success();
    }

    @ApiOperation("删除报修")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        repairService.removeById(id);
        return Result.success();
    }
}
