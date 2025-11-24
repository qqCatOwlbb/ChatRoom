package com.catowl.chatroom.service;

import com.catowl.chatroom.model.VO.VideoStatsVO;

import java.util.List;
import java.util.Map;

/**
 * @program: ChatRoom
 * @description: 视频：用户的互动行为，动态的行为管理
 * @author: qqCatOwlbb
 * @create: 2025-11-22 09:56
 **/
public interface InteractionService {
    /**
    * @Description: 增加播放量
    * @Param: [videoId]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    void incrementViewCount(Long videoId);

    /**
    * @Description: 点赞 / 取消点赞
    * @Param: [userId, videoId]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    void toggleLike(Long userId, Long videoId);

    /**
    * @Description: 批量获取视频互动数据（播放量，点赞量，是否已点赞）
    * @Param: [videoIds, currentUserId]
    * @return: java.util.Map<java.lang.Long,com.catowl.chatroom.model.VO.VideoStatsVO>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    Map<Long, VideoStatsVO> getStats(List<Long> videoIds, Long currentUserId);
}
