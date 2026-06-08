package com.dormitory.util;

import com.dormitory.common.BusinessException;
import com.dormitory.common.ResultCode;
import lombok.Data;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录抛异常
     */
    public static LoginUser getRequired() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long getUserId() {
        return getRequired().getUserId();
    }

    public static String getRole() {
        return getRequired().getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息
     */
    @Data
    public static class LoginUser {
        private Long userId;
        private String username;
        private String role;

        public LoginUser(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }
}
