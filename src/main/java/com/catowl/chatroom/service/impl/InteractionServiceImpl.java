package com.catowl.chatroom.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.ServerException;
import com.catowl.chatroom.mapper.MqExceptionLogMapper;
import com.catowl.chatroom.mapper.VideoMapper;
import com.catowl.chatroom.model.DTO.request.LikeEventRequest;
import com.catowl.chatroom.model.VO.VideoStatsVO;
import com.catowl.chatroom.model.entity.MqExceptionLog;
import com.catowl.chatroom.model.entity.Video;
import com.catowl.chatroom.service.HotVideoService;
import com.catowl.chatroom.service.InteractionService;
import com.catowl.chatroom.service.VideoService;
import jdk.jpackage.internal.Log;
import lombok.Data;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: ChatRoom
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-11-22 10:31
 **/
@Service
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotVideoService hotVideoService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    @Qualifier("viewIncrScript")
    private DefaultRedisScript<Long> viewIncrScript;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private MqExceptionLogMapper mqExceptionLogMapper;

    @Autowired
    @Qualifier("likeToggleScript")
    private DefaultRedisScript<Long> likeToggleScript;

    private static final String VIDEO_SCORE_DIRTY_KEY = "video:score:dirty";
    private static final String VIDEO_DB_DIRTY_KEY = "video:db:dirty";

    private static final String VIDEO_VIEW_COUNT_KEY = "video:view:";
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:";
    private static final String USER_LIKE_KEY = "user:likes:";
    private static final String LOCK_KEY_PREFIX = "lock:video:rebuild:";

    private static final String LIKE_EVENT_TOPIC = "video-like-topic";

    @Override
    public void incrementViewCount(Long videoId) {
        String redisKey = VIDEO_VIEW_COUNT_KEY + videoId;
        for (int i = 0; i < 10; i++) {
            Long result = redisTemplate.execute(
                    viewIncrScript,
                    Arrays.asList(redisKey, VIDEO_SCORE_DIRTY_KEY, VIDEO_DB_DIRTY_KEY),
                    videoId
            );
            if (result != -1) {
                return;
            }
            initVideoCount(videoId, redisKey, "view");
        }
        throw new ServerException(ExceptionEnum.REDIS_ERROR, "浏览量回源失败或lua脚本执行失败");
    }

    @Override
    public void toggleLike(Long userId, Long videoId) {
        String userLikeKey = USER_LIKE_KEY + userId;
        String videoLikeKey = VIDEO_LIKE_COUNT_KEY + videoId;
        for (int i = 0; i < 10; i++) {
            Long result = redisTemplate.execute(
                    likeToggleScript,
                    Arrays.asList(userLikeKey, videoLikeKey, VIDEO_SCORE_DIRTY_KEY, VIDEO_DB_DIRTY_KEY),
                    videoId
            );
            if(result != -1){
                return;
            }
            initVideoCount(videoId, videoLikeKey, "like");
        }
        throw new ServerException(ExceptionEnum.REDIS_ERROR,"点赞量回源失败或lua脚本执行失败");
    }

    @Override
    public Map<Long, VideoStatsVO> getStats(List<Long> videoIds, Long currentUserId) {
        Map<Long, VideoStatsVO> map = new HashMap<>();
        if(videoIds == null || videoIds.isEmpty()){
            return map;
        }

        // 获取video的点赞，播放量，（可选）查询用户点赞与否
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<Object>() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for(Long vid : videoIds){
                    operations.opsForValue().get(VIDEO_VIEW_COUNT_KEY + vid);
                    operations.opsForValue().get(VIDEO_LIKE_COUNT_KEY + vid);

                    if(currentUserId != null){
                        operations.opsForSet().isMember(USER_LIKE_KEY + currentUserId, vid);
                    }
                }
                return null;
            }
        });

        // 步长
        int step = (currentUserId == null) ? 2 : 3;
        for(int i = 0; i < videoIds.size(); i++){
            Long vid = videoIds.get(i);
            int index = i * step;

            Object viewObj = results.get(index);
            Object likeObj = results.get(index + 1);

            long views = (viewObj instanceof Number) ? ((Number) viewObj).longValue() : 0L;
            long likes = (likeObj instanceof Number) ? ((Number) likeObj).longValue() : 0L;

            boolean isLiked = false;
            if (currentUserId != null){
                Object isLikedObj = results.get(index + 2);
                // 一种防御性操作，在确认完其是否为boolean后再转换
                isLiked = (isLikedObj instanceof Boolean) && (Boolean) isLikedObj;
            }

            map.put(vid, new VideoStatsVO(views, likes, isLiked));
        }

        return map;
    }

    private void initVideoCount(Long videoId, String redisKey, String type){
        String lockKey = LOCK_KEY_PREFIX + videoId;
        RLock rLock = redissonClient.getLock(lockKey);

        // 进锁
        try{
            boolean isLocked = rLock.tryLock(500, -1, TimeUnit.MILLISECONDS);

            if(isLocked){
                try {
                    if (redisTemplate.hasKey(redisKey)) {
                        // 前面拿到锁的已经干完活了，后面拿到锁的就可以直接滚了
                        return;
                    }
                    Video video = videoMapper.findById(videoId);
                    if (video == null) {
                        // 防穿透
                        redisTemplate.opsForValue().set(redisKey, -1, 1, TimeUnit.MINUTES);
                        return;
                    }
                    long ttl = 7 * 24 * 3600 + (long) (Math.random() * 3600);
                    long count = "view".equals(type) ? video.getViewCount() : video.getLikeCount();
                    redisTemplate.opsForValue().set(redisKey, count, ttl, TimeUnit.SECONDS);
                } finally {
                    rLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(ExceptionEnum.REDIS_ERROR, "获取分布式锁被中断" + e);
        }
    }

    /**
    * @Description: Mq生产者，将点赞消息发往mq
    * @Param: [userId]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/25
    */
    private void sendLikeEventToMq(Long userId, Long videoId, Integer action){
        LikeEventRequest likeEventRequest = new LikeEventRequest(userId,videoId,action,System.currentTimeMillis());
        // hashKey保证同一用户对同一视频的点赞行为在同一队列，保证顺序性
        String hashKey = userId + ":" + videoId;

        try {
            rocketMQTemplate.syncSendOrderly(
                    LIKE_EVENT_TOPIC,
                    MessageBuilder.withPayload(likeEventRequest).build(),
                    hashKey
            );
        }catch (Exception e){
            try{
                MqExceptionLog log = new MqExceptionLog();
                log.setTopic(LIKE_EVENT_TOPIC);
                log.setHashKey(hashKey);
                log.setJsonContent(JSONObject.toJSONString(likeEventRequest));
                // 怕太长了
                log.setErrorMsg(e.getMessage().substring(0,Math.min(e.getMessage().length(),500)));
                mqExceptionLogMapper.insertLog(log);
            }catch (Exception dbEx){
                throw new ServerException(ExceptionEnum.DATABASE_ERROR,"严重事故：MQ发送失败且本地异常表写入失败! Event:"+dbEx);
            }
            throw new ServerException(ExceptionEnum.MQ_ERROR,"MQ发送失败，降级写入本地异常表:" + e);
        }
    }
}
