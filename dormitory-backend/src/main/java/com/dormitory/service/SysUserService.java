package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.common.PageResult;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.SysUser;

/**
 * 用户服务
 */
public interface SysUserService extends IService<SysUser> {

    /** 分页查询用户（可按角色、关键字过滤） */
    PageResult<SysUser> page(PageQuery query, String role);

    /** 新增用户（密码加密） */
    void add(SysUser user);

    /** 修改用户（不改密码） */
    void edit(SysUser user);

    /** 重置密码为 123456 */
    void resetPassword(Long id);

    /** 修改自己的密码 */
    void changePassword(Long userId, String oldPwd, String newPwd);
}
