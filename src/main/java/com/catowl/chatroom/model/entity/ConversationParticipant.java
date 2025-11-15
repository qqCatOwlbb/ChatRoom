package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 会话参与者实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipant {
    private Long id;

    private Long conversationId;

    private Long userId;

    private Long lastReadSequenceId;

    private LocalDateTime joinedAt;
}
