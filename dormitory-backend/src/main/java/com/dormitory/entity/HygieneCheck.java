package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 卫生检查实体
 */
@Data
@TableName("hygiene_check")
public class HygieneCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Long checkerId;

    private Integer score;

    private String comment;

    private LocalDate checkDate;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
