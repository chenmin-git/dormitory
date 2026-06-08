package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.ai.SparkService;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PageResult;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Repair;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.RepairMapper;
import com.dormitory.service.RepairService;
import com.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报修服务实现
 */
@Service
@RequiredArgsConstructor
public class RepairServiceImpl extends ServiceImpl<RepairMapper, Repair> implements RepairService {

    private final StudentService studentService;
    private final SparkService sparkService;
    private final BedMapper bedMapper;

    @Override
    public void submit(Long currentUserId, Repair repair) {
        Student student = studentService.getByUserId(currentUserId);
        if (student == null) {
            throw new BusinessException("当前账号未关联学生信息，无法报修");
        }
        repair.setId(null);
        repair.setStudentId(student.getId());
        repair.setStatus("待派单");
        repair.setHandlerId(null);
        repair.setFinishTime(null);

        // 未指定房间时，自动关联学生所住房间
        if (repair.getRoomId() == null && student.getBedId() != null) {
            Bed bed = bedMapper.selectById(student.getBedId());
            if (bed != null) {
                repair.setRoomId(bed.getRoomId());
            }
        }

        // AI 智能分类（失败有兜底，不阻塞）
        String[] result = sparkService.classifyRepair(repair.getTitle(), repair.getDescription());
        repair.setAiCategory(result[0]);
        repair.setAiPriority(result[1]);

        save(repair);
    }

    @Override
    public PageResult<Map<String, Object>> page(Integer pageNum, Integer pageSize, String status,
                                                Long studentIdFilter) {
        IPage<Map<String, Object>> page = baseMapper.pageDetail(
                new Page<>(pageNum, pageSize), studentIdFilter, status);
        return PageResult.of(page);
    }

    @Override
    public void assign(Long repairId, Long handlerId) {
        Repair repair = getById(repairId);
        if (repair == null) {
            throw new BusinessException("报修单不存在");
        }
        repair.setHandlerId(handlerId);
        repair.setStatus("维修中");
        updateById(repair);
    }

    @Override
    public void finish(Long repairId, String remark) {
        Repair repair = getById(repairId);
        if (repair == null) {
            throw new BusinessException("报修单不存在");
        }
        repair.setStatus("已完成");
        repair.setHandleRemark(remark);
        repair.setFinishTime(LocalDateTime.now());
        updateById(repair);
    }
}
