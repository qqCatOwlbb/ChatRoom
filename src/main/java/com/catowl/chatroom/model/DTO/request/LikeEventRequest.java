package com.catowl.chatroom.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: ChatRoom
 * @description: 点赞关系的请求体（MQ）
 * @author: qqCatOwlbb
 * @create: 2025-11-25 19:04
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEventRequest implements Serializable {
    private Long userId;
    private Long videoId;
    private Integer action;
    private Long timestamp;
}
