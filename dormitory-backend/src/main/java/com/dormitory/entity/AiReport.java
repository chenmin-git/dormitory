package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 数据分析报告历史
 */
@Data
@TableName("ai_report")
public class AiReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报告内容（Markdown） */
    private String content;

    private Long creatorId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
