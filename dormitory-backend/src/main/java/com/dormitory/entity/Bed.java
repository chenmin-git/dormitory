package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 床位实体
 */
@Data
@TableName("bed")
public class Bed {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private String bedNo;

    /** 入住学生id，空=未分配 */
    private Long studentId;

    /** 0 空闲 1 已住 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
