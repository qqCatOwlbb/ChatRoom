package com.catowl.chatroom.task;

import com.catowl.chatroom.mapper.VideoMapper;
import com.catowl.chatroom.model.entity.Video;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @program: ChatRoom
 * @description: 点赞浏览量同步任务
 * @author: qqCatOwlbb
 * @create: 2025-11-24 21:04
 **/
@Component
@EnableScheduling
@Slf4j
public class InteractionSyncTask {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    @Qualifier("renameDirtyScript")
    private DefaultRedisScript<Long> renameScript;

    private static final String DIRTY_KEY = "video:db:dirty";
    private static final String PROCESSING_KEY = "video:db:dirty";
    private static final String VIEW_KEY_PREFIX = "video:view:";
    private static final String LIKE_KEY_PREFIX = "video:like:";

    @Scheduled(fixedRate = 10000)
    public void syncToMysql(){
        Long result = redisTemplate.execute(
                renameScript,
                Arrays.asList(DIRTY_KEY, PROCESSING_KEY)
        );

        if(result == 0 || result == 2){
            return;
        }

        Set<Object> dirtyIds = redisTemplate.opsForSet().members(PROCESSING_KEY);
        if(dirtyIds == null || dirtyIds.isEmpty()){
            return;
        }

        List<Video> batchUpdateList = new ArrayList<>();

        List<Object> results = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for(Object idObj : dirtyIds){
                    String id = idObj.toString();
                    operations.opsForValue().get(VIEW_KEY_PREFIX + id);
                    operations.opsForValue().get(LIKE_KEY_PREFIX + id);
                }
                return null;
            }
        });

        int index = 0;
        for(Object idObj : dirtyIds){
            Long vid = Long.parseLong(idObj.toString());
            Object viewCountObj = results.get(index++);
            Object likeCountObj = results.get(index++);
            if(viewCountObj != null && likeCountObj != null){
                Video video = new Video();
                video.setId(vid);
                video.setViewCount(Long.parseLong(viewCountObj.toString()));
                video.setLikeCount(Long.parseLong(likeCountObj.toString()));
                batchUpdateList.add(video);
            }
        }

        if(!batchUpdateList.isEmpty()){
            videoMapper.batchUpdateScores(batchUpdateList);
            log.info("同步了{}条视频互动数据", batchUpdateList.size());
        }

        redisTemplate.delete(PROCESSING_KEY);
    }
}
