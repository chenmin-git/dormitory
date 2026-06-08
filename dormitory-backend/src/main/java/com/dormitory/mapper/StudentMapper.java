package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /** 分页学生列表，含住宿位置（楼栋-房间-床位） */
    @Select("<script>" +
            "SELECT s.*, b.name AS buildingName, r.room_no AS roomNo, bd.bed_no AS bedNo " +
            "FROM student s " +
            "LEFT JOIN bed bd ON s.bed_id = bd.id " +
            "LEFT JOIN room r ON bd.room_id = r.id " +
            "LEFT JOIN building b ON r.building_id = b.id " +
            "WHERE s.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (s.real_name LIKE CONCAT('%',#{keyword},'%') OR s.student_no LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY s.id DESC" +
            "</script>")
    IPage<Map<String, Object>> pageWithLocation(IPage<?> page, @Param("keyword") String keyword);
}
