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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String VIDEO_INFO_KEY_PREFIX = "video:info:";

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

    }

    @Override
    public List<VideoFeedVO> getFeedList(VideoFeedRequest request, Long currentUserId) {
        return null;
    }

    @Override
    public List<VideoFeedVO> getHotVideoList(Long currentUserId) {
        List<Long> videoIds = hotVideoService.getTopHotVideoIds(50);
        List<VideoFeedVO> videoList = getVideoDetailsBatch(videoIds);
        Map<Long, VideoStatsVO> statsMap = interactionService.getStats(videoIds, currentUserId);

        for(VideoFeedVO video : videoList){
            VideoStatsVO stats = statsMap.get(video.getInternalId());
            if(stats != null){
                video.setLikeCount(stats.getLikeCount());
                video.setViewCount(stats.getViewCount());
                video.setLiked(stats.getIsLiked());
            }
        }
        return videoList;
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

    /**
    * @Description: 获取视频的详情信息
    * @Param: [videoIds]
    * @return: java.util.List<com.catowl.chatroom.model.VO.VideoFeedVO>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/24
    */
    private List<VideoFeedVO> getVideoDetailsBatch(List<Long> videoIds){
        // 1. 组装redisKey
        List<String> redisKey = videoIds.stream()
                .map(id -> VIDEO_INFO_KEY_PREFIX + videoIds)
                .collect(Collectors.toList());

        // 2. 查表(pipeline)
        List<Object> cacheObj = redisTemplate.opsForValue().multiGet(redisKey);
        
        List<VideoFeedVO> resultList = new ArrayList<>(videoIds.size());
        List<Long> missIds = new ArrayList<>();
        Map<Long, VideoFeedVO> hitMap = new HashMap<>();
        // 3. 分流
        int i = 0;
        for (Object o : cacheObj) {
            if(o instanceof VideoFeedVO){
                hitMap.put(videoIds.get(i), (VideoFeedVO) o);
            }else{
                missIds.add(videoIds.get(i));
            }
            i++;
        }

        // 4. 补缺
        if(!missIds.isEmpty()){
            List<VideoFeedVO> dbList = videoMapper.selectFeedVOByIds(missIds);
            Map<Long, VideoFeedVO> dbMap = dbList.stream()
                    .collect(Collectors.toMap(VideoFeedVO::getInternalId, Function.identity(),(k1, k2) -> k1) );
            // 写回redis
            if(!dbList.isEmpty()){
                redisTemplate.executePipelined(new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        if(!dbList.isEmpty()){
                            for(VideoFeedVO video : dbList){
                                String key = VIDEO_INFO_KEY_PREFIX + video.getInternalId();
                                long ttl = 60 * 60 * 24 + (long) (Math.random() * 3600);
                                redisTemplate.opsForValue().set(key, video, ttl, TimeUnit.SECONDS);
                            }
                        }
                        return null;
                    }
                });
            }
            hitMap.putAll(dbMap);
        }

        // 按照顺序放回
        for(Long id : videoIds){
            VideoFeedVO video = hitMap.get(id);
            resultList.add(video);
        }
        return resultList;
    }
}
