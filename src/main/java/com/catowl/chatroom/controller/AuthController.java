package com.catowl.chatroom.controller;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.model.DTO.request.LoginRequest;
import com.catowl.chatroom.model.DTO.request.RegisterRequest;
import com.catowl.chatroom.model.DTO.response.LoginResponse;
import com.catowl.chatroom.model.DTO.response.RefreshResponse;
import com.catowl.chatroom.model.DTO.response.ResultResponse;
import com.catowl.chatroom.service.AuthService;
import com.catowl.chatroom.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * @program: ChatRoom
 * @description: 登陆验证 Controller
 * @author: qqCatOwlbb
 * @create: 2025-11-15 15:15
 **/
@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpirationMs;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @PostMapping("/register")
    public ResultResponse<String> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResultResponse.success();
    }

    @PostMapping("/login")
    public ResultResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response){
        AuthService.LoginResult loginResult = authService.login(loginRequest);
        long cookieMaxAgeSeconds = TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpirationMs);
        CookieUtil.setHttpOnlyCookie(response, REFRESH_TOKEN_COOKIE_NAME, loginResult.getRefreshToken(), cookieMaxAgeSeconds);
        return ResultResponse.success(new LoginResponse(loginResult.getAccessToken()));
    }

    @PostMapping("/refresh")
    public ResultResponse<RefreshResponse> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken){
        if (refreshToken == null) {
            throw new BusinessException(ExceptionEnum.TOKEN_NOT_PROVIDED);
        }
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        return ResultResponse.success(new RefreshResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public ResultResponse<String> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken, HttpServletResponse response){
        authService.logout(refreshToken);
        CookieUtil.clearCookie(response, REFRESH_TOKEN_COOKIE_NAME);
        return ResultResponse.success();
    }
}
