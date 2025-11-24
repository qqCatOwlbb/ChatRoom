package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.ForbiddenException;
import com.catowl.chatroom.exception.NotFoundException;
import com.catowl.chatroom.mapper.VideoMapper;
import com.catowl.chatroom.model.DTO.internal.UserSimpleInfo;
import com.catowl.chatroom.model.DTO.request.VideoFeedRequest;
import com.catowl.chatroom.model.DTO.request.VideoUpdateRequest;
import com.catowl.chatroom.model.VO.VideoStatsVO;
import com.catowl.chatroom.model.VO.VideoFeedVO;
import com.catowl.chatroom.model.entity.Video;
import com.catowl.chatroom.service.HotVideoService;
import com.catowl.chatroom.service.InteractionService;
import com.catowl.chatroom.service.UserService;
import com.catowl.chatroom.service.VideoService;
import com.catowl.chatroom.utils.AliOssUtil;
import com.catowl.chatroom.utils.UlidUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: ChatRoom
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-11-23 11:09
 **/
@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserService userService;             // 独立的 UserService

    @Autowired
    private InteractionService interactionService; // 独立的互动服务

    @Autowired
    private HotVideoService hotVideoService;     // 独立的热门服务

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public void uploadVideo(Long userId, String title, String description, MultipartFile videoFile, MultipartFile coverFile) {
        if (videoFile == null || videoFile.isEmpty()) throw new BusinessException(ExceptionEnum.FILE_EMPTY);

        try {
            String videoPath = "videos/" + UlidUtils.generate() + videoFile.getOriginalFilename().substring(videoFile.getOriginalFilename().lastIndexOf("."));
            aliOssUtil.upload(videoFile.getInputStream(), videoPath); // 私有上传

            String coverPath = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                coverPath = "covers/" + UlidUtils.generate() + videoFile.getOriginalFilename().substring(videoFile.getOriginalFilename().lastIndexOf("."));
                aliOssUtil.upload(coverFile.getInputStream(), coverPath);
            }

            Video video = new Video();
            video.setUlid(UlidUtils.generate());
            video.setUploaderUserId(userId);
            video.setTitle(title);
            video.setDescription(description);
            video.setVideoUrl(videoPath);
            video.setCoverImageUrl(coverPath);
            video.setStatus("pending");

            videoMapper.insertVideo(video);
        } catch (IOException e) {
            throw new BusinessException(ExceptionEnum.FILE_STORAGE_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteVideo(Long userId, String videoUlid) {
        Video video =
    }

    @Override
    public List<VideoFeedVO> getFeedList(VideoFeedRequest request, Long currentUserId) {
        return List.of();
    }

    @Override
    public List<VideoFeedVO> getHotVideoList(Long currentUserId) {
        return List.of();
    }

    @Override
    public VideoFeedVO getVideoDetail(Long currentUserId, String videoUlid) {
        return null;
    }

    @Override
    public Video findByUlid(String ulid) {
        return null;
    }

    @Override
    public void updateVideoInfo(Long userId, String videoUlid, VideoUpdateRequest request) {

    }

    @Override
    public void updateVideoCover(Long userId, String videoUlid, MultipartFile newCoverFile) {

    }
}
