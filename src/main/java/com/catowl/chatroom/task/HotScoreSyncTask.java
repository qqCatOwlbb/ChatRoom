package com.catowl.chatroom.task;

import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.ServerException;
import com.catowl.chatroom.service.HotVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: ChatRoom
 * @description: 热门榜单定时更新任务
 * @author: qqCatOwlbb
 * @create: 2025-11-22 11:47
 **/
@Component
@EnableScheduling
@Slf4j
public class HotScoreSyncTask {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotVideoService hotVideoService;

    @Autowired
    @Qualifier("renameDirtyScript")
    private DefaultRedisScript<Long> renameScript;

    private static final String VIDEO_SCORE_DIRTY_KEY = "video:score:dirty";
    private static final String VIDEO_SCORE_PROCESSING_KEY = "video:score:processing";

    @Scheduled(fixedRate = 5000)
    public void syncHotScore(){
        List<String> keys = Arrays.asList(VIDEO_SCORE_DIRTY_KEY, VIDEO_SCORE_PROCESSING_KEY);

        Long status = redisTemplate.execute(
                renameScript,
                keys
        );

        // 通过状态码判断此次有没有进行搬运，要不要进行更新
        if(status == 0){
            // 不搬运，不更新
            log.info("没有要搬运的数据");
            return;
        }else if(status == 2){
            // 不搬运，要更新
            log.warn("仍有未更新完的数据，本次不做搬运");
        }else{
            //要搬运，要更新
            log.info("已执行完搬运任务，即将开始更新");
        }

        processBatch();
    }

    private void processBatch(){
        try{
            Set<Object> idsObj = redisTemplate.opsForSet().members(VIDEO_SCORE_PROCESSING_KEY);
            if(idsObj == null || idsObj.isEmpty()){
                // 按理来说是不会空了，但还是防御一下吧
                return;
            }
            Set<Long> videoIds = idsObj.stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(Collectors.toSet());
            hotVideoService.updateHotScoresBatch(videoIds);
            redisTemplate.delete(VIDEO_SCORE_PROCESSING_KEY);
            log.info("热度更新完成，处理数量：{}", videoIds.size());
        }catch (Exception e){
            throw new ServerException(ExceptionEnum.REDIS_ERROR, "热度更新异常，数据保留在processing中，等待下一次定时任务");
        }
    }
}
