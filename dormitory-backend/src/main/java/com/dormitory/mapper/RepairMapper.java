package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.Repair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RepairMapper extends BaseMapper<Repair> {

    /** 按 AI 分类统计报修数量 */
    @Select("SELECT ai_category AS name, COUNT(*) AS value FROM repair WHERE deleted = 0 GROUP BY ai_category")
    List<Map<String, Object>> countByCategory();

    /** 分页报修列表，含学生姓名、房间号、处理人；studentId 非空时仅查该学生 */
    @Select("<script>" +
            "SELECT rp.*, s.real_name AS studentName, s.student_no AS studentNo, " +
            "r.room_no AS roomNo, h.real_name AS handlerName " +
            "FROM repair rp " +
            "LEFT JOIN student s ON rp.student_id = s.id " +
            "LEFT JOIN room r ON rp.room_id = r.id " +
            "LEFT JOIN sys_user h ON rp.handler_id = h.id " +
            "WHERE rp.deleted = 0 " +
            "<if test='studentId != null'> AND rp.student_id = #{studentId} </if>" +
            "<if test='status != null and status != \"\"'> AND rp.status = #{status} </if>" +
            "ORDER BY rp.id DESC" +
            "</script>")
    IPage<Map<String, Object>> pageDetail(IPage<?> page,
                                          @Param("studentId") Long studentId,
                                          @Param("status") String status);
}
