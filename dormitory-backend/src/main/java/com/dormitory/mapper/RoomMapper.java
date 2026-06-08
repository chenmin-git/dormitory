package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /** 分页房间列表，含楼栋名与已住人数 */
    @Select("<script>" +
            "SELECT r.*, b.name AS buildingName, " +
            "(SELECT COUNT(*) FROM bed bd WHERE bd.room_id = r.id AND bd.status = 1 AND bd.deleted = 0) AS occupied " +
            "FROM room r LEFT JOIN building b ON r.building_id = b.id " +
            "WHERE r.deleted = 0 " +
            "<if test='buildingId != null'> AND r.building_id = #{buildingId} </if>" +
            "ORDER BY r.building_id, r.room_no" +
            "</script>")
    IPage<Map<String, Object>> pageWithBuilding(IPage<?> page, @Param("buildingId") Long buildingId);
}
