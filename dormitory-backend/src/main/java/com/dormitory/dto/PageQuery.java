package com.dormitory.dto;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** 通用关键字 */
    private String keyword;
}
