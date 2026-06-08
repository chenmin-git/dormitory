package com.dormitory.service;

import java.util.Map;

/**
 * 统计服务
 */
public interface StatService {

    /** 概览统计（数字卡片 + 图表数据） */
    Map<String, Object> overview();
}
