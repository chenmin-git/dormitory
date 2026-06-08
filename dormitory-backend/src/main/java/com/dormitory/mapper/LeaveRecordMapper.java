package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.LeaveRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface LeaveRecordMapper extends BaseMapper<LeaveRecord> {

    /** 分页请假/晚归列表，含学生姓名、审批人；studentId 非空时仅查该学生 */
    @Select("<script>" +
            "SELECT lr.*, s.real_name AS studentName, s.student_no AS studentNo, a.real_name AS approverName " +
            "FROM leave_record lr " +
            "LEFT JOIN student s ON lr.student_id = s.id " +
            "LEFT JOIN sys_user a ON lr.approver_id = a.id " +
            "WHERE lr.deleted = 0 " +
            "<if test='studentId != null'> AND lr.student_id = #{studentId} </if>" +
            "<if test='status != null and status != \"\"'> AND lr.status = #{status} </if>" +
            "ORDER BY lr.id DESC" +
            "</script>")
    IPage<Map<String, Object>> pageDetail(IPage<?> page,
                                          @Param("studentId") Long studentId,
                                          @Param("status") String status);
}
