package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PageResult;
import com.dormitory.entity.LeaveRecord;
import com.dormitory.entity.Student;
import com.dormitory.mapper.LeaveRecordMapper;
import com.dormitory.service.LeaveService;
import com.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 请假/晚归服务实现
 */
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends ServiceImpl<LeaveRecordMapper, LeaveRecord> implements LeaveService {

    private final StudentService studentService;

    @Override
    public void submit(Long currentUserId, LeaveRecord record) {
        Student student = studentService.getByUserId(currentUserId);
        if (student == null) {
            throw new BusinessException("当前账号未关联学生信息，无法申请");
        }
        record.setId(null);
        record.setStudentId(student.getId());
        record.setStatus("待审批");
        record.setApproverId(null);
        record.setApproveRemark(null);
        save(record);
    }

    @Override
    public PageResult<Map<String, Object>> page(Integer pageNum, Integer pageSize, String status,
                                                Long studentIdFilter) {
        IPage<Map<String, Object>> page = baseMapper.pageDetail(
                new Page<>(pageNum, pageSize), studentIdFilter, status);
        return PageResult.of(page);
    }

    @Override
    public void approve(Long id, Long approverId, boolean pass, String remark) {
        LeaveRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("申请记录不存在");
        }
        record.setStatus(pass ? "通过" : "驳回");
        record.setApproverId(approverId);
        record.setApproveRemark(remark);
        updateById(record);
    }
}
