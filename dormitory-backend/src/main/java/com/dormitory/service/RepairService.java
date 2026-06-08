package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.common.PageResult;
import com.dormitory.entity.Repair;

import java.util.Map;

/**
 * 报修服务
 */
public interface RepairService extends IService<Repair> {

    /** 学生提交报修（触发 AI 智能分类） */
    void submit(Long currentUserId, Repair repair);

    /** 分页查询。currentUserId 对应学生时仅看自己的报修 */
    PageResult<Map<String, Object>> page(Integer pageNum, Integer pageSize, String status,
                                         Long studentIdFilter);

    /** 派单（指定处理人，状态置维修中） */
    void assign(Long repairId, Long handlerId);

    /** 完成维修 */
    void finish(Long repairId, String remark);
}
