package com.catowl.chatroom.model.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 刷新 Token 响应体
 * @author: qqCatOwlbb
 * @create: 2025-11-15 16:49
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {
    private String accessToken;
}
