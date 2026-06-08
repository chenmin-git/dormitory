package com.dormitory.service;

import com.dormitory.dto.LoginDTO;
import com.dormitory.entity.SysUser;
import com.dormitory.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /** 登录 */
    LoginVO login(LoginDTO dto);

    /** 获取当前登录用户信息 */
    SysUser currentUser();
}
