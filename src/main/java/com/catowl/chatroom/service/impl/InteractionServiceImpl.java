package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.model.VO.VideoStatsVO;
import com.catowl.chatroom.service.HotVideoService;
import com.catowl.chatroom.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String VIDEO_SCORE_DIRTY_KEY = "video:score:dirty";

    private static final String VIDEO_VIEW_COUNT_KEY = "video:view:";
    private static final String VIDEO_LIKE_COUNT_KEY = "video:like:";
    private static final String USER_LIKE_KEY = "user:likes:";

    @Override
    public void incrementViewCount(Long videoId) {
        Long views = redisTemplate.opsForValue().increment(VIDEO_VIEW_COUNT_KEY + videoId);
        // 标记该视频的数据脏了
        redisTemplate.opsForSet().add(VIDEO_SCORE_DIRTY_KEY, videoId);
    }

    @Override
    public void toggleLike(Long userId, Long videoId) {
        String userLikeKey = USER_LIKE_KEY + userId;
        String videoLikeCountKey = VIDEO_LIKE_COUNT_KEY + videoId;
        // 查看用户有没有点赞
        Boolean isMember = redisTemplate.opsForSet().isMember(userLikeKey, videoId);
        Object viewsObj = redisTemplate.opsForValue().get(VIDEO_VIEW_COUNT_KEY + videoId);
        long views = (viewsObj == null) ? 0 : Long.parseLong(viewsObj.toString());

        if (Boolean.TRUE.equals(isMember)) {
            // 取消点赞
            redisTemplate.opsForSet().remove(userLikeKey, videoId);
            redisTemplate.opsForValue().decrement(videoLikeCountKey);
        } else {
            redisTemplate.opsForSet().add(userLikeKey, videoId);
            redisTemplate.opsForValue().increment(videoLikeCountKey);
        }

        redisTemplate.opsForSet().add(VIDEO_SCORE_DIRTY_KEY, videoId);
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
}
