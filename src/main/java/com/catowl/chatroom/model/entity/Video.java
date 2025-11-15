package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 视频实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:04
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Video extends BaseEntity{

    private Long uploaderUserId;

    private String title;

    private String videoUrl;

    private String coverImageUrl;

    /**
     * 对应数据库: `status` ENUM('pending', 'approved', 'rejected')
     * 在Java中用 String 映射 ENUM 是最简单可靠的
     */
    private String Status;

    private String auditNotes;

    private Long auditorUserId;

    private LocalDateTime auditedAt;

    private LocalDateTime uploadedAt;
}
