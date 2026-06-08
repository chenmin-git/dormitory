package com.dormitory.service;

/**
 * 入住分配服务：床位与学生的绑定/解绑
 */
public interface AllocationService {

    /** 将学生分配到指定床位 */
    void allocate(Long studentId, Long bedId);

    /** 学生退宿（释放其床位） */
    void deallocate(Long studentId);
}
