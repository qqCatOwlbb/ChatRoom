package com.catowl.chatroom.controller;

import com.catowl.chatroom.model.DTO.request.LoginRequest;
import com.catowl.chatroom.model.DTO.request.RegisterRequest;
import com.catowl.chatroom.model.DTO.response.LoginResponse;
import com.catowl.chatroom.model.DTO.response.ResultResponse;
import com.catowl.chatroom.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    @PostMapping("/register")
    public ResultResponse<String> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);
        return ResultResponse.success();
    }

    @PostMapping("/login")
    public ResultResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResultResponse.success(loginResponse);
    }
}
