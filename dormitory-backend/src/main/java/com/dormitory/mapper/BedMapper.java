package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    /** 某房间的床位列表，含入住学生姓名、学号 */
    @Select("SELECT bd.*, s.real_name AS studentName, s.student_no AS studentNo " +
            "FROM bed bd LEFT JOIN student s ON bd.student_id = s.id " +
            "WHERE bd.room_id = #{roomId} AND bd.deleted = 0 ORDER BY bd.bed_no")
    List<Map<String, Object>> listByRoom(Long roomId);
}
