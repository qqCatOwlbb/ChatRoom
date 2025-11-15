package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * @program: ChatRoom
 * @description: 用户实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-14 15:22
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity{
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private Integer status;
}
