package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FAQ 知识库实体
 */
@Data
@TableName("faq")
public class Faq {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String question;

    private String answer;

    private String category;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
