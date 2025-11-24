package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.init.BloomFilterInit;
import com.catowl.chatroom.mapper.UserMapper;
import com.catowl.chatroom.model.DTO.internal.UserSimpleInfo;
import com.catowl.chatroom.model.entity.User;
import com.catowl.chatroom.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<Long> userBloomFilter;

    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    @PostConstruct
    public void init(){
        this.userBloomFilter = redissonClient.getBloomFilter(BloomFilterInit.USER_ID_BLOOM_KEY);
    }

    @Override
    public Map<Long, UserSimpleInfo> getUserSimpleInfos(List<Long> userIds) {
        if(userIds == null || userIds.isEmpty()){
            return new HashMap<>();
        }

        // 去重
        List<Long> distinctIds = userIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if(distinctIds.isEmpty()){
            return new HashMap<>();
        }

        List<Long> validIds = new ArrayList<>();
        for(Long id : distinctIds){
            if(userBloomFilter.contains(id)){
                validIds.add(id);
            }else{
                throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
            }
        }

        if(validIds.isEmpty()){
            return new HashMap<>();
        }

        // 包装
        List<String> keys = validIds.stream()
                .map(id -> USER_INFO_KEY_PREFIX + id)
                .collect(Collectors.toList());

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<Long> missIds = new ArrayList<>();
        Map<Long, UserSimpleInfo> resultMap = new HashMap<>();

        // 查漏
        for(int i = 0; i < validIds.size(); i++){
            Long uid = validIds.get(i);
            // 防御式写法：检查查出的values是否为空，避免NullPointerException，检查i是否在values的有效范围内，避免IndexOutOfBoundsException
            Object val = (values != null && values.size() > i) ? values.get(i) : null;
            if(val != null){
                UserSimpleInfo info = (UserSimpleInfo) val;

                // 如果布隆过滤器误判了，则采用“缓存空对象”机制
                // 如果缓存的是空对象（ID = -1），则不上报也不加入missIds
                if(!info.getId().equals(-1L)){
                    resultMap.put(uid, info);
                }
            }else{
                missIds.add(uid);
            }
        }

        // 补缺
        if(!missIds.isEmpty()){
            List<User> dbUsers = userMapper.selectByIds(missIds);
            Map<Long, User> dbUserMap = dbUsers.stream().collect(Collectors.toMap(User::getId, u -> u));

            Map<String, UserSimpleInfo> cacheMap = new HashMap<>();

            for(Long uid : missIds){
                User u = dbUserMap.get(uid);
                String key = USER_INFO_KEY_PREFIX + uid;
                if(u != null){
                    UserSimpleInfo info = new UserSimpleInfo(u.getId(),u.getUlid(),u.getNickname(),u.getAvatarUrl());
                    resultMap.put(uid, info);
                    cacheMap.put(key,info);
                }else{
                    // 布隆过滤器误判，我们要存空对象，防止它下次还访问得到数据库
                    cacheMap.put(key,new UserSimpleInfo(-1L,null,null,null));
                }
            }
            if(!cacheMap.isEmpty()){
                redisTemplate.executePipelined(new SessionCallback<Object>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        for (Map.Entry<String, UserSimpleInfo> entry : cacheMap.entrySet()){
                            String key = entry.getKey();
                            UserSimpleInfo val = entry.getValue();
                            if(val.getId().equals(-1L)){
                                // 空值短存
                                operations.opsForValue().set(key,val,300,TimeUnit.SECONDS);
                            }else{
                                // 加入随机时长，应对缓存雪崩
                                long ttl = 24 * 3600 + (long) (Math.random() * 3600);
                                operations.opsForValue().set(key,val,ttl,TimeUnit.SECONDS);
                            }
                        }
                        return null;
                    }
                });
            }
        }
        return resultMap;
    }
}
