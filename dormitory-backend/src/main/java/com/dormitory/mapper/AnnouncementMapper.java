package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /** 分页公告，含发布人姓名 */
    @Select("SELECT a.*, u.real_name AS publisherName " +
            "FROM announcement a LEFT JOIN sys_user u ON a.publisher_id = u.id " +
            "WHERE a.deleted = 0 ORDER BY a.id DESC")
    IPage<Map<String, Object>> pageDetail(IPage<?> page);
}
