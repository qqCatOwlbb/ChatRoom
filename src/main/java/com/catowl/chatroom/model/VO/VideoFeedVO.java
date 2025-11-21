package com.catowl.chatroom.model.VO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 视频流查询
 * @author: qqCatOwlbb
 * @create: 2025-11-21 09:45
 **/
@Data
public class VideoFeedVO {
    // 视频相关信息
    private String videoId;       // 对应 video.ulid
    private String title;
    private String description;
    private String videoUrl;
    private String coverImageUrl;
    private LocalDateTime uploadedAt;

    // 作者相关信息 (通过联表查询获得)
    private String uploaderId;        // 对应 user_account.ulid
    private String uploaderNickname;  // 对应 user_account.nickname
    private String uploaderAvatar;    // 对应 user_account.avatar_url
}