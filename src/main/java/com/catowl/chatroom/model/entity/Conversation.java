package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 会话实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:09
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {
    private String type;

    private String lastMessageContent;

    private Long lastMessageSenderId;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;
}
