package com.catowl.chatroom.task;

import com.alibaba.fastjson.JSONObject;
import com.catowl.chatroom.mapper.MqExceptionLogMapper;
import com.catowl.chatroom.model.DTO.request.LikeEventRequest;
import com.catowl.chatroom.model.entity.MqExceptionLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: ChatRoom
 * @description: Mq补偿任务
 * @author: qqCatOwlbb
 * @create: 2025-11-25 20:01
 **/
@Component
@EnableScheduling
@Slf4j
public class MqCompensationTask {
    @Autowired
    private MqExceptionLogMapper mqExceptionLogMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Scheduled(fixedRate = 30000)
    public void retryFailedMessage(){
        List<MqExceptionLog> logs = mqExceptionLogMapper.selectPendingLogs();
        if(logs.isEmpty()){
            return;
        }
        log.info("开始执行 MQ 补偿任务，待处理数量：{}",logs.size());

        for(MqExceptionLog logEvent : logs){
            try {
                LikeEventRequest event = JSONObject.parseObject(logEvent.getJsonContent(), LikeEventRequest.class);
                rocketMQTemplate.syncSendOrderly(
                        logEvent.getTopic(),
                        MessageBuilder.withPayload(event).build(),
                        logEvent.getHashKey()
                );
                mqExceptionLogMapper.updateStatusSuccess(logEvent.getId());
            }catch (Exception e){
                // 又失败了，给它的尝试次数+1，交给以后的定时任务再处理，超过5次就不由定时任务负责了
                log.warn("MQ 补偿重试失败, id: {}", logEvent.getId(), e);
                mqExceptionLogMapper.incrementRetryCount(logEvent.getId());
            }
        }
    }
}
