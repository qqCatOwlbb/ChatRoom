package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.mapper.UserMapper;
import com.catowl.chatroom.model.DTO.internal.UserSimpleInfo;
import com.catowl.chatroom.model.entity.User;
import com.catowl.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: ChatRoom
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-11-21 17:19
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    @Override
    public Map<Long, UserSimpleInfo> getUserSimpleInfos(List<Long> userIds) {
        if(userIds == null || userIds.isEmpty()){
            return new HashMap<>();
        }

        // 去重
        List<Long> distinctIds = userIds.stream().distinct().collect(Collectors.toList());

        // 包装
        List<String> keys = distinctIds.stream()
                .map(id -> USER_INFO_KEY_PREFIX + id)
                .collect(Collectors.toList());

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<Long> missIds = new ArrayList<>();
        Map<Long, UserSimpleInfo> resultMap = new HashMap<>();

        // 查漏
        for(int i = 0; i < distinctIds.size(); i++){
            Long uid = distinctIds.get(i);
            // 防御式写法：检查查出的values是否为空，避免NullPointerException，检查i是否在values的有效范围内，避免IndexOutOfBoundsException
            Object val = (values != null && values.size() > i) ? values.get(i) : null;
            if(val != null){
                resultMap.put(uid, (UserSimpleInfo) val);
            }else{
                missIds.add(uid);
            }
        }

        // 补缺
        if(!missIds.isEmpty()){
            List<User> dbUsers = userMapper.selectByIds(missIds);

            Map<String, UserSimpleInfo> cacheMap = new HashMap<>();

            for(User u : dbUsers){
                UserSimpleInfo info = new UserSimpleInfo(u.getId(),u.getUlid(),u.getNickname(),u.getAvatarUrl());
                resultMap.put(u.getId(),info);
                cacheMap.put(USER_INFO_KEY_PREFIX + u.getId(),info);
            }
            if(!cacheMap.isEmpty()){
                redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        for (Map.Entry<String, UserSimpleInfo> entry : cacheMap.entrySet()){
                            long baseTtl = 24 * 60 * 60;
                            long jitter = (long) (Math.random() * 60 * 60);
                            operations.opsForValue().set(
                                    entry.getKey(),
                                    entry.getValue(),
                                    baseTtl + jitter,
                                    TimeUnit.SECONDS
                            );
                        }
                        return null;
                    }
                });
            }
        }
        return resultMap;
    }
}
