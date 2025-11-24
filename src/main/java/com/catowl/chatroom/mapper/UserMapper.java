package com.catowl.chatroom.mapper;

import com.catowl.chatroom.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<User> selectByIds(List<Long> ids);
    
    /** 
    * @Description: 用于布隆过滤器的预热
    * @Param: []
    * @return: java.util.List<java.lang.Long>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/21
    */
    List<Long> findAllIds();
    
    /** 
    * @Description: 布隆过滤器预热
    * @Param: []
    * @return: java.util.List<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/21
    */
    List<String> findAllUsernames();
}
