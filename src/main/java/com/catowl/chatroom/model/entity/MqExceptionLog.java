package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 本地消息异常实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-25 19:22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqExceptionLog {
    private Long id;
    private String topic;
    private String hashKey;
    private String jsonContent;
    private String errorMsg;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
