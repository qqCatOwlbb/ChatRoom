package com.catowl.chatroom.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 游标请求
 * @author: qqCatOwlbb
 * @create: 2025-11-21 09:34
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoFeedRequest {
    private String title;
    private String cursor;
    private Integer limit = 10;
}
