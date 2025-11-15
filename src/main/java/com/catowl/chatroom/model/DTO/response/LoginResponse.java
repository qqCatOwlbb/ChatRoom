package com.catowl.chatroom.model.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 登录成功响应体
 * @author: qqCatOwlbb
 * @create: 2025-11-15 15:13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
