package com.dormitory.controller;

import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.Student;
import com.dormitory.service.StudentService;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生管理
 */
@Api(tags = "学生管理")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @ApiOperation("分页查询学生(含住宿位置)")
    @GetMapping("/page")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<PageResult<Map<String, Object>>> page(PageQuery query) {
        return Result.success(studentService.page(query));
    }

    @ApiOperation("新增学生(自动建账号)")
    @PostMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    @ApiOperation("修改学生")
    @PutMapping
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> edit(@RequestBody Student student) {
        student.setUserId(null);
        student.setBedId(null);
        studentService.updateById(student);
        return Result.success();
    }

    @ApiOperation("删除学生")
    @DeleteMapping("/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success();
    }

    @ApiOperation("获取当前登录学生的个人信息")
    @GetMapping("/me")
    public Result<Student> me() {
        return Result.success(studentService.getByUserId(UserContext.getUserId()));
    }
}
