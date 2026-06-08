package com.dormitory.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类：生成、解析 token
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";

    /**
     * 生成 token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 解析 token，返回 Claims；解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserId(Claims claims) {
        return claims.get(CLAIM_USER_ID, Number.class).longValue();
    }

    public String getUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public String getRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
