package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 群聊消息实体
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:23
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupProfile {
    private Long conversationId;

    private String groupName;

    private Long ownerUserId;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
