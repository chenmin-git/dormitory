package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dormitory.entity.AiReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface AiReportMapper extends BaseMapper<AiReport> {

    /** 分页历史报告，含生成人姓名 */
    @Select("SELECT r.*, u.real_name AS creatorName " +
            "FROM ai_report r LEFT JOIN sys_user u ON r.creator_id = u.id " +
            "WHERE r.deleted = 0 ORDER BY r.id DESC")
    IPage<Map<String, Object>> pageDetail(IPage<?> page);
}
