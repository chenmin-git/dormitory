package com.dormitory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.Building;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.service.BuildingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 楼栋服务实现
 */
@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

    @Override
    public List<Map<String, Object>> listWithManager() {
        return baseMapper.listWithManager();
    }
}
