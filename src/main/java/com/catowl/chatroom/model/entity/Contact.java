package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 联系人关系实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private Long id;

    private Long ownerUserId;

    private Long contactUserId;

    private String alias;

    private String status;

    private LocalDateTime createdAt;
}
