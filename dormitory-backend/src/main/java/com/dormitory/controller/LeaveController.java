package com.dormitory.controller;

import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.LeaveRecord;
import com.dormitory.entity.Student;
import com.dormitory.service.LeaveService;
import com.dormitory.service.StudentService;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 请假/晚归管理
 */
@Api(tags = "请假/晚归管理")
@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final StudentService studentService;

    @ApiOperation("学生提交请假/晚归申请")
    @PostMapping
    @PreAuth(RoleConstant.STUDENT)
    public Result<Void> submit(@RequestBody LeaveRecord record) {
        leaveService.submit(UserContext.getUserId(), record);
        return Result.success();
    }

    @ApiOperation("分页查询(学生仅看自己)")
    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long studentFilter = null;
        if (RoleConstant.STUDENT.equals(UserContext.getRole())) {
            Student student = studentService.getByUserId(UserContext.getUserId());
            studentFilter = student != null ? student.getId() : -1L;
        }
        return Result.success(leaveService.page(pageNum, pageSize, status, studentFilter));
    }

    @ApiOperation("审批")
    @PutMapping("/approve")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> approve(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        boolean pass = Boolean.parseBoolean(body.get("pass").toString());
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        leaveService.approve(id, UserContext.getUserId(), pass, remark);
        return Result.success();
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        leaveService.removeById(id);
        return Result.success();
    }
}
