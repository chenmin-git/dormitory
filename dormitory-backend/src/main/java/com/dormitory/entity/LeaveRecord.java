package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 请假/晚归记录实体
 */
@Data
@TableName("leave_record")
public class LeaveRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 请假 / 晚归 */
    private String type;

    private String reason;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 待审批/通过/驳回 */
    private String status;

    private Long approverId;

    private String approveRemark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
