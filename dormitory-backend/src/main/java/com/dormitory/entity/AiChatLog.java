package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话记录实体
 */
@Data
@TableName("ai_chat_log")
public class AiChatLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String answer;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
