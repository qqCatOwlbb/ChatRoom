package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.model.VO.VideoFeedVO;
import com.catowl.chatroom.service.HotVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ChatRoom
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-11-22 10:03
 **/
@Service
public class HotVideoServiceImpl implements HotVideoService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_RANK_KEY = "video:hot_rank";
    private static final String VIDEO_VIEW_COUNT_KEY = "video:view:";
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:";

    @Override
    public void updateHotScoresBatch(Set<Long> videoIds) {
        if(videoIds == null || videoIds.isEmpty()){
            return;
        }

        List<Object> stats = redisTemplate.executePipelined(new SessionCallback<Object>() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for(Long vid : videoIds){
                    operations.opsForValue().get(VIDEO_VIEW_COUNT_KEY + vid);
                    operations.opsForValue().get(VIDEO_LIKE_COUNT_KEY + vid);
                }
                return null;
            }
        });

        Set<ZSetOperations.TypedTuple<Object>> tupleSet = new HashSet<>();

        int index = 0;
        for(Long vid : videoIds){
            Object viewObj = stats.get(index++);
            Object likeObj = stats.get(index++);

            long views = (viewObj instanceof Number) ? ((Number) viewObj).longValue() : 0L;
            long likes = (likeObj instanceof Number) ? ((Number) likeObj).longValue() : 0L;

            double score = (views * 1.0) + (likes * 5.0);

            tupleSet.add(new DefaultTypedTuple<>(vid.toString(), score));
        }

        if(!tupleSet.isEmpty()){
            redisTemplate.opsForZSet().add(HOT_RANK_KEY, tupleSet);
        }
    }

    @Override
    public List<Long> getTopHotVideoIds(int limits) {
        Set<Object> ids = redisTemplate.opsForZSet().reverseRange(HOT_RANK_KEY, 0, limits - 1);
        if(ids == null || ids.isEmpty()){
            return Collections.emptyList();
        }
        return ids.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public void removeVideoFromRank(Long videoId) {
        redisTemplate.opsForZSet().remove(HOT_RANK_KEY, videoId);
    }

}
