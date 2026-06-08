package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PageResult;
import com.dormitory.common.RoleConstant;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.Student;
import com.dormitory.entity.SysUser;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.AllocationService;
import com.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 学生服务实现
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final AllocationService allocationService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<Map<String, Object>> page(PageQuery query) {
        IPage<Map<String, Object>> page = baseMapper.pageWithLocation(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getKeyword());
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Student student) {
        long exist = lambdaQuery().eq(Student::getStudentNo, student.getStudentNo()).count();
        if (exist > 0) {
            throw new BusinessException("学号已存在");
        }
        // 用学号作为登录账号创建用户
        long userExist = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, student.getStudentNo()));
        if (userExist > 0) {
            throw new BusinessException("该学号对应的登录账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(student.getStudentNo());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRealName(student.getRealName());
        user.setRole(RoleConstant.STUDENT);
        user.setGender(student.getGender());
        user.setPhone(student.getPhone());
        user.setStatus(1);
        sysUserMapper.insert(user);

        student.setId(null);
        student.setUserId(user.getId());
        student.setBedId(null);
        save(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Student student = getById(id);
        if (student == null) {
            return;
        }
        // 释放床位
        if (student.getBedId() != null) {
            allocationService.deallocate(id);
        }
        // 删除登录账号
        if (student.getUserId() != null) {
            sysUserMapper.deleteById(student.getUserId());
        }
        removeById(id);
    }

    @Override
    public Student getByUserId(Long userId) {
        return lambdaQuery().eq(Student::getUserId, userId).one();
    }
}
