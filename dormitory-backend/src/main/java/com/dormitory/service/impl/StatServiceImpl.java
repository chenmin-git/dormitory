package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import com.dormitory.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StudentMapper studentMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final RepairMapper repairMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final HygieneCheckMapper hygieneCheckMapper;
    private final BuildingMapper buildingMapper;

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();

        // 数字卡片
        long studentCount = studentMapper.selectCount(null);
        long roomCount = roomMapper.selectCount(null);
        long bedTotal = bedMapper.selectCount(null);
        long bedUsed = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery().eq(Bed::getStatus, 1));
        long pendingRepair = repairMapper.selectCount(
                Wrappers.<Repair>lambdaQuery().ne(Repair::getStatus, "已完成"));
        long pendingLeave = leaveRecordMapper.selectCount(
                Wrappers.<LeaveRecord>lambdaQuery().eq(LeaveRecord::getStatus, "待审批"));

        result.put("studentCount", studentCount);
        result.put("roomCount", roomCount);
        result.put("bedTotal", bedTotal);
        result.put("bedUsed", bedUsed);
        result.put("occupancyRate", bedTotal == 0 ? 0 : Math.round(bedUsed * 1000.0 / bedTotal) / 10.0);
        result.put("pendingRepair", pendingRepair);
        result.put("pendingLeave", pendingLeave);

        // 报修分类占比（饼图）
        List<Map<String, Object>> repairByCategory = repairMapper.countByCategory();
        result.put("repairByCategory", repairByCategory);

        // 各楼栋入住情况（柱状图）
        result.put("buildingOccupancy", buildingMapper.occupancyByBuilding());

        // 报修状态分布
        Map<String, Object> repairStatus = new HashMap<>();
        for (String s : new String[]{"待派单", "维修中", "已完成"}) {
            repairStatus.put(s, repairMapper.selectCount(
                    Wrappers.<Repair>lambdaQuery().eq(Repair::getStatus, s)));
        }
        result.put("repairStatus", repairStatus);

        // 卫生平均分
        List<HygieneCheck> checks = hygieneCheckMapper.selectList(null);
        double avgScore = checks.stream().mapToInt(HygieneCheck::getScore).average().orElse(0);
        result.put("hygieneAvgScore", Math.round(avgScore * 10) / 10.0);

        // 请假/晚归数量
        long leaveCount = leaveRecordMapper.selectCount(
                Wrappers.<LeaveRecord>lambdaQuery().eq(LeaveRecord::getType, "请假"));
        long lateCount = leaveRecordMapper.selectCount(
                Wrappers.<LeaveRecord>lambdaQuery().eq(LeaveRecord::getType, "晚归"));
        result.put("leaveCount", leaveCount);
        result.put("lateCount", lateCount);

        return result;
    }
}
