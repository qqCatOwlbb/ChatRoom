package com.catowl.chatroom.mapper;

import com.catowl.chatroom.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @program: ChatRoom
 * @description: 用户逻辑操作的mapper层
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:27
 **/
@Mapper
public interface UserMapper {
    /** 
    * @Description: 根据用户名查找用户
    * @Param: [username]
    * @return: com.catowl.chatroom.model.entity.User
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    User findByUsername(@Param("username") String username);

    /** 
    * @Description: 插入新用户
    * @Param: [user]
    * @return: int
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    int insertUser(User user);
}
