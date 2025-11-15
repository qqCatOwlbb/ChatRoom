package com.catowl.chatroom.service;

import com.catowl.chatroom.model.DTO.request.LoginRequest;
import com.catowl.chatroom.model.DTO.request.RegisterRequest;
import com.catowl.chatroom.model.DTO.response.LoginResponse;

/**
 * @program: ChatRoom
 * @description: 登录验证接口
 * @author: qqCatOwlbb
 * @create: 2025-11-15 15:18
 **/
public interface AuthService {

    /**
    * @Description: 注册接口
    * @Param: [registerRequest]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    void register(RegisterRequest registerRequest);

    /**
    * @Description: 登录接口
    * @Param: [loginRequest]
    * @return: com.catowl.chatroom.model.DTO.response.LoginResponse
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    LoginResponse login(LoginRequest loginRequest);
    
    /** 
    * @Description: 注销
    * @Param: []
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    void logout();

    /**
    * @Description: 刷新 AccessToken
    * @Param: [refreshToken]
    * @return: java.lang.String
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    String refreshAccessToken(String refreshToken);
}
