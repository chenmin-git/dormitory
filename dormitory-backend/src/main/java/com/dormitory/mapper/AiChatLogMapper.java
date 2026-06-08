package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.AiChatLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatLogMapper extends BaseMapper<AiChatLog> {

    /** 某用户最近 N 条对话，按时间正序返回（用于会话回显） */
    @Select("SELECT * FROM (" +
            "  SELECT * FROM ai_chat_log WHERE user_id = #{userId} AND deleted = 0 ORDER BY id DESC LIMIT #{limit}" +
            ") t ORDER BY t.id ASC")
    List<AiChatLog> recentByUser(Long userId, Integer limit);
}
