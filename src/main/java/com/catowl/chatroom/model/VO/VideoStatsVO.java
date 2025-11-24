package com.catowl.chatroom.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: ChatRoom
 * @description: 视频互动数据 VO
 * @author: qqCatOwlbb
 * @create: 2025-11-22 10:24
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatsVO {
    private Long viewCount;
    private Long likeCount;
    private Boolean isLiked;
}
