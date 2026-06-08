package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修实体
 */
@Data
@TableName("repair")
public class Repair {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long roomId;

    private String title;

    private String description;

    /** AI 分类：水电/网络/家具/门窗/其他 */
    private String aiCategory;

    /** AI 优先级：紧急/一般/低 */
    private String aiPriority;

    /** 待派单/维修中/已完成 */
    private String status;

    /** 处理人(宿管)id */
    private Long handlerId;

    private String handleRemark;

    private LocalDateTime finishTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
