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

import java.beans.Transient;
import java.net.PasswordAuthentication;
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
    public LoginResponse login(LoginRequest loginRequest) {
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
        String refreshToken = jwtUtil.generateRefreshToken(
                securityUser.getUser().getId()
        );

        redisCache.redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + securityUser.getUser().getId(),
                securityUser,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser)) {
            // 如果没有认证信息(比如匿名访问)，或者 Principal 不是 SecurityUser，
            // 可能是因为 AT 已过期但未被拦截，此时无需任何操作
            return;
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long userId = securityUser.getUser().getId();

        redisCache.redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + userId);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        if(!jwtUtil.validateToken(refreshToken)){
            throw new BusinessException(ExceptionEnum.TOKEN_INVALID);
        }

        Long userId;
        try{
            userId = jwtUtil.getUserIdFromClaims(jwtUtil.getClaimsFromToken(refreshToken))
        }
    }


}
