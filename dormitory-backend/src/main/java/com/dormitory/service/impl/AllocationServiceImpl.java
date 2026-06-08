package com.dormitory.service.impl;

import com.dormitory.common.BusinessException;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入住分配服务实现
 */
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final BedMapper bedMapper;
    private final StudentMapper studentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocate(Long studentId, Long bedId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            throw new BusinessException("床位不存在");
        }
        if (bed.getStudentId() != null && !bed.getStudentId().equals(studentId)) {
            throw new BusinessException("该床位已被占用");
        }

        // 释放学生原床位
        if (student.getBedId() != null && !student.getBedId().equals(bedId)) {
            freeBed(student.getBedId());
        }

        // 绑定新床位
        bed.setStudentId(studentId);
        bed.setStatus(1);
        bedMapper.updateById(bed);

        student.setBedId(bedId);
        studentMapper.updateById(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deallocate(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        if (student.getBedId() != null) {
            freeBed(student.getBedId());
            studentMapper.update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers
                    .<Student>lambdaUpdate()
                    .set(Student::getBedId, null)
                    .eq(Student::getId, studentId));
        }
    }

    /** 清空床位入住信息（student_id 置空需用 UpdateWrapper，否则 MP 忽略 null） */
    private void freeBed(Long bedId) {
        bedMapper.update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers
                .<Bed>lambdaUpdate()
                .set(Bed::getStudentId, null)
                .set(Bed::getStatus, 0)
                .eq(Bed::getId, bedId));
    }
}
