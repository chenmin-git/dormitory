package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 楼栋实体
 */
@Data
@TableName("building")
public class Building {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 男 / 女 */
    private String genderType;

    /** 宿管员用户id */
    private Long managerId;

    private Integer floorCount;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
