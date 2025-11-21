package com.catowl.chatroom.service;

import com.catowl.chatroom.model.DTO.internal.UserSimpleInfo;

import java.util.List;
import java.util.Map;

/**
 * @program: ChatRoom
 * @description: 用户信息管理
 * @author: qqCatOwlbb
 * @create: 2025-11-21 17:14
 **/
public interface UserService {
    Map<Long, UserSimpleInfo> getUserSimpleInfos(List<Long> userIds);
}
