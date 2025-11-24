package com.catowl.chatroom.service;

import com.catowl.chatroom.model.DTO.request.VideoFeedRequest;
import com.catowl.chatroom.model.DTO.request.VideoUpdateRequest;
import com.catowl.chatroom.model.VO.VideoFeedVO;
import com.catowl.chatroom.model.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @program: ChatRoom
 * @description: 视频：基础属性，静态的档案管理器
 * @author: qqCatOwlbb
 * @create: 2025-11-22 09:55
 **/
public interface VideoService {
    /** 
    * @Description: 上传视频
    * @Param: [userId, title, description, videoFile, coverFile]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    void uploadVideo(Long userId, String title, String description, MultipartFile videoFile, MultipartFile coverFile);
    
    /** 
    * @Description: 删除视频
    * @Param: [userId, videoUlid]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    void deleteVideo(Long userId, String videoUlid);

    /**
    * @Description: 获取视频流（首页）
    * @Param: [request, currentUserId]
    * @return: java.util.List<com.catowl.chatroom.model.VO.VideoFeedVO>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    List<VideoFeedVO> getFeedList(VideoFeedRequest request, Long currentUserId);

    /**
    * @Description: 获取热门榜单
    * @Param: [currentUserId]
    * @return: java.util.List<com.catowl.chatroom.model.VO.VideoFeedVO>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    List<VideoFeedVO> getHotVideoList(Long currentUserId);

    /**
    * @Description: 根据视频 ID 获取本人的视频详情
    * @Param: [currentUserId, videoUlid]
    * @return: com.catowl.chatroom.model.VO.VideoFeedVO
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    VideoFeedVO getVideoDetail(Long currentUserId, String videoUlid);

    /**
    * @Description: 根据视频 ID 获取视频详情
    * @Param: [ulid]
    * @return: com.catowl.chatroom.model.entity.Video
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    Video findByUlid(String ulid);

    /**
    * @Description: 更新视频信息
    * @Param: [userId, videoUlid, request]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    void updateVideoInfo(Long userId, String videoUlid, VideoUpdateRequest request);

    /**
    * @Description: 更新视频封面
    * @Param: [userId, videoUlid, newCoverFile]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/23
    */
    void updateVideoCover(Long userId, String videoUlid, MultipartFile newCoverFile);
}
