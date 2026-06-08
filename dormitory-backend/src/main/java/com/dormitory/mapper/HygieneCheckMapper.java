package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.HygieneCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface HygieneCheckMapper extends BaseMapper<HygieneCheck> {

    /** 分页卫生检查，含楼栋名、房间号、检查人 */
    @Select("SELECT hc.*, b.name AS buildingName, r.room_no AS roomNo, u.real_name AS checkerName " +
            "FROM hygiene_check hc " +
            "LEFT JOIN room r ON hc.room_id = r.id " +
            "LEFT JOIN building b ON r.building_id = b.id " +
            "LEFT JOIN sys_user u ON hc.checker_id = u.id " +
            "WHERE hc.deleted = 0 ORDER BY hc.check_date DESC, hc.id DESC")
    IPage<Map<String, Object>> pageDetail(IPage<?> page);
}
