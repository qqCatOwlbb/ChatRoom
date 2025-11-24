package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.init.BloomFilterInit;
import com.catowl.chatroom.mapper.RoleMapper;
import com.catowl.chatroom.mapper.UserMapper;
import com.catowl.chatroom.model.entity.Role;
import com.catowl.chatroom.model.entity.SecurityUser;
import com.catowl.chatroom.model.entity.User;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * @program: ChatRoom
 * @description: 拦截链中的查询数据库验证方法
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:25
 **/
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<String> nameBloomFilter;

    @PostConstruct
    public void init(){
        this.nameBloomFilter = redissonClient.getBloomFilter(BloomFilterInit.USER_NAME_BLOOM_KEY);
    }

    /**
    * @Description: 重写 loadUserByUsername 方法
    * @Param: [username]
    * @return: org.springframework.security.core.userdetails.UserDetails
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 防穿透
        if(!nameBloomFilter.contains(username)){
            throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        }
        User user = userMapper.findByUsername(username);
        if(user == null){
            throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        }
        Collection<Role> roles = roleMapper.findRolesByUserId(user.getId());

        return new SecurityUser(user, roles);
    }
}
