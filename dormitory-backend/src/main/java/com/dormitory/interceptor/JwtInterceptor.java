package com.dormitory.interceptor;

import com.dormitory.common.BusinessException;
import com.dormitory.common.PreAuth;
import com.dormitory.common.ResultCode;
import com.dormitory.util.JwtUtil;
import com.dormitory.util.UserContext;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * JWT 登录拦截器 + 角色校验
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行非控制器请求（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 解析 token
        String authHeader = request.getHeader(header);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(prefix)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(prefix.length());
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 写入上下文
        String role = jwtUtil.getRole(claims);
        UserContext.set(new UserContext.LoginUser(
                jwtUtil.getUserId(claims),
                jwtUtil.getUsername(claims),
                role));

        // 角色校验
        HandlerMethod method = (HandlerMethod) handler;
        PreAuth preAuth = method.getMethodAnnotation(PreAuth.class);
        if (preAuth == null) {
            preAuth = method.getBeanType().getAnnotation(PreAuth.class);
        }
        if (preAuth != null && preAuth.value().length > 0) {
            boolean allowed = Arrays.asList(preAuth.value()).contains(role);
            if (!allowed) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
