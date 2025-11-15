package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.mapper.RoleMapper;
import com.catowl.chatroom.mapper.UserMapper;
import com.catowl.chatroom.model.DTO.request.LoginRequest;
import com.catowl.chatroom.model.DTO.request.RegisterRequest;
import com.catowl.chatroom.model.DTO.response.LoginResponse;
import com.catowl.chatroom.model.entity.Role;
import com.catowl.chatroom.model.entity.SecurityUser;
import com.catowl.chatroom.model.entity.User;
import com.catowl.chatroom.service.AuthService;
import com.catowl.chatroom.utils.JwtUtil;
import com.catowl.chatroom.utils.RedisCache;
import com.catowl.chatroom.utils.UlidUtils;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.beans.Transient;
import java.net.PasswordAuthentication;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: ChatRoom
 * @description: 登录验证实现类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 15:21
 **/
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisCache redisCache;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpirationMs;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "rt:";

    private static final String USER_SESSIONS_KEY_PREFIX = "refresh_tokens_for_user:";

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if(userMapper.findByUsername(registerRequest.getUsername()) != null){
            throw new BusinessException(ExceptionEnum.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUlid(UlidUtils.generate());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setStatus(1);

        userMapper.insertUser(user);
        Long newUserId = user.getId();

        Role defaultRole = roleMapper.findRoleByName("ROLE_USER");
        if(defaultRole == null) {
            throw new BusinessException(ExceptionEnum.INTERNAL_SERVER_ERROR, "默认角色ROLE_USER未在数据库中定义！");
        }

        roleMapper.insertUserRole(newUserId,defaultRole.getId());
    }

    @Override
    public LoginResult login(LoginRequest loginRequest) {
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword())
            );
        }catch (BadCredentialsException e){
            throw new BusinessException(ExceptionEnum.PASSWORD_WRONG);
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(
                securityUser.getUser().getId(),
                securityUser.getAuthorities()
        );
        String refreshToken = UUID.randomUUID().toString().replace("-","");

        String userIndexKey = USER_SESSIONS_KEY_PREFIX + securityUser.getUser().getId();

        redisCache.redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + refreshToken,
                securityUser,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        redisCache.redisTemplate.opsForSet().add(userIndexKey, refreshToken);
        redisCache.expire(userIndexKey, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);

        return new LoginResult(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        if(!StringUtils.hasText(refreshToken)){
            throw new BusinessException(ExceptionEnum.TOKEN_NOT_PROVIDED);
        }
        String sessionKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        SecurityUser securityUser = (SecurityUser) redisCache.getCacheObject(sessionKey);
        redisCache.redisTemplate.delete(sessionKey);
        if (securityUser != null) {
            String userIndexKey = USER_SESSIONS_KEY_PREFIX + securityUser.getUser().getId();
            // 4. 从索引 Set 中移除这个 RT
            redisCache.redisTemplate.opsForSet().remove(userIndexKey, refreshToken);
        }
    }


    @Override
    public String refreshAccessToken(String refreshToken) {

        String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        SecurityUser securityUser = (SecurityUser) redisCache.getCacheObject(redisKey);
        if(securityUser == null){
            throw new BusinessException(ExceptionEnum.TOKEN_INVALID, "会话已失效，请重新登录");
        }else{
            System.out.println("找得到"+securityUser.getUser().getId().toString());
        }

        String newAccessToken = jwtUtil.generateAccessToken(
                securityUser.getUser().getId(),
                securityUser.getAuthorities()
        );
        redisCache.expire(redisKey, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
        String userIndexKey = USER_SESSIONS_KEY_PREFIX + securityUser.getUser().getId();
        redisCache.expire(userIndexKey, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
        return newAccessToken;
    }


}
