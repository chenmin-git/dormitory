package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.common.PageResult;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.Student;

import java.util.Map;

/**
 * 学生服务
 */
public interface StudentService extends IService<Student> {

    /** 分页（含住宿位置） */
    PageResult<Map<String, Object>> page(PageQuery query);

    /** 新增学生（同步创建登录账号，账号=学号，密码=123456） */
    void add(Student student);

    /** 删除学生（同步删除账号、释放床位） */
    void delete(Long id);

    /** 根据登录用户id获取学生信息 */
    Student getByUserId(Long userId);
}
