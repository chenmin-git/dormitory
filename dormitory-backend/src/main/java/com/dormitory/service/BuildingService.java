package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.Building;

import java.util.List;
import java.util.Map;

/**
 * 楼栋服务
 */
public interface BuildingService extends IService<Building> {

    /** 楼栋列表（含宿管姓名） */
    List<Map<String, Object>> listWithManager();
}
