package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.Building;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {

    /** 楼栋列表，关联宿管姓名 */
    @Select("SELECT b.*, u.real_name AS managerName " +
            "FROM building b LEFT JOIN sys_user u ON b.manager_id = u.id " +
            "WHERE b.deleted = 0 ORDER BY b.id")
    List<Map<String, Object>> listWithManager();

    /** 各楼栋床位总数与已住数（看板用） */
    @Select("SELECT b.name AS name, " +
            "(SELECT COUNT(*) FROM bed bd JOIN room r ON bd.room_id = r.id WHERE r.building_id = b.id AND bd.deleted = 0) AS total, " +
            "(SELECT COUNT(*) FROM bed bd JOIN room r ON bd.room_id = r.id WHERE r.building_id = b.id AND bd.status = 1 AND bd.deleted = 0) AS used " +
            "FROM building b WHERE b.deleted = 0 ORDER BY b.id")
    List<Map<String, Object>> occupancyByBuilding();
}
