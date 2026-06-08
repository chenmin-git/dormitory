package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.common.PageResult;
import com.dormitory.entity.LeaveRecord;

import java.util.Map;

/**
 * 请假/晚归服务
 */
public interface LeaveService extends IService<LeaveRecord> {

    /** 学生提交申请 */
    void submit(Long currentUserId, LeaveRecord record);

    /** 分页查询。studentIdFilter 非空时仅看该学生 */
    PageResult<Map<String, Object>> page(Integer pageNum, Integer pageSize, String status,
                                         Long studentIdFilter);

    /** 审批 */
    void approve(Long id, Long approverId, boolean pass, String remark);
}
