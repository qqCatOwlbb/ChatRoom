package com.catowl.chatroom.model.DTO.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 轻量级用户信息 redis 缓存对象
 * @author: qqCatOwlbb
 * @create: 2025-11-21 16:27
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleInfo {
    private Long id;
    private String ulid;
    private String nickname;
    private String avatarUrl;
}
