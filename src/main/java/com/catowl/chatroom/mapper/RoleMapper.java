package com.catowl.chatroom.mapper;

import com.catowl.chatroom.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @program: ChatRoom
 * @description: 角色mapper
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:51
 **/
@Mapper
public interface RoleMapper {
    /**
    * @Description: 根据用户内部ID查找角色列表
    * @Param: [userId]
    * @return: java.util.Collection<com.catowl.chatroom.model.entity.Role>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    Collection<Role> findRolesByUserId(Long userId);

    /**
    * @Description: 根据角色名称查找角色
    * @Param: [roleName]
    * @return: com.catowl.chatroom.model.entity.Role
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    Role findRoleByName(String roleName);

    /** 
    * @Description: 为用户分配角色
    * @Param: [userId, roleId]
    * @return: int
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") int roleId);
}
