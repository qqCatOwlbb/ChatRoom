package com.catowl.chatroom.utils;

import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1. 创建jwt工具类
 * */
@Component // 保持 @Component 注解
public class JwtUtil {

    // --- 1. 注入的配置 (非静态) ---

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpirationMs;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpirationMs;

    // --- 2. 实例字段 (非静态) ---

    private SecretKey key; // 将 key 变为实例字段

    // --- 3. 初始化方法 ---

    /**
    * @Description: 生成密匙
    * @Param: []
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    @PostConstruct
    public void init() {
        // 基于注入的 secretKeyString 来生成 SecretKey
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // --- 4. 公共方法 (全部改为非静态) ---

    /**
    * @Description: 生成access-token
    * @Param: [userId, authorities]
    * @return: java.lang.String
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    public String generateAccessToken(Long userId, Collection<? extends GrantedAuthority> authorities) {
        // 将权限列表转换为 List<String>
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 存入 Long id
                .claim("roles", roles) // 存入权限
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256) // 使用实例的 key
                .compact();
    }

    /**
    * @Description: 解析token，获取所有的claims
    * @Param: [token]
    * @return: io.jsonwebtoken.Claims
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // 使用实例的 key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ExceptionEnum.TOKEN_INVALID);
        }
    }

    /**
    * @Description: 从claims中获得用户ID
    * @Param: [claims]
    * @return: java.lang.Long
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    public Long getUserIdFromClaims(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
    * @Description: 验证签名和时效
    * @Param: [token]
    * @return: boolean
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    public boolean validateToken(String token) {
        try {
            // getClaimsFromToken 内部已经包含了所有验证
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ExceptionEnum.TOKEN_INVALID);
        }
    }
}
