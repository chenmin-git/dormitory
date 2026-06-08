package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间实体
 */
@Data
@TableName("room")
public class Room {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buildingId;

    private String roomNo;

    private Integer floor;

    private Integer capacity;

    /** 1 可用 0 停用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
