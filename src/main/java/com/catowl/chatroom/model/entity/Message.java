package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 消息实体类，此表不继承BaseEntity，因为它的ulid由客户端生成，用于幂等性
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long id;

    private String ulid;

    private Long conversationId;

    private Long sequenceId;

    private Long senderUserId;

    private String content;

    private LocalDateTime createdAt;
}
