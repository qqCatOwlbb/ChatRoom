package com.catowl.chatroom.mapper;

import com.catowl.chatroom.model.entity.MqExceptionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @program: ChatRoom
 * @description: 异常表，Mq是异步操作，和redis，mysql同步不同，且多为网络请求，
 * 出错了用户请求可能接收不到错误，导致数据库或缓存的操作没能即使回滚，所以需要有异常表
 * 保证出错了也能弥补保证信息的一致
 * @author: qqCatOwlbb
 * @create: 2025-11-25 19:44
 **/
@Mapper
public interface MqExceptionLogMapper {
    void insertLog(MqExceptionLog log);
    List<MqExceptionLog> selectPendingLogs();
    int updateStatusSuccess(@Param("id") Long id);
    int incrementRetryCount(@Param("id") Long id);
}
