package com.catowl.chatroom.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @program: ChatRoom
 * @description: 角色实体类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 14:01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

    private int id;

    private String name;

    @Override
    @JsonIgnore
    public String getAuthority() {
        return this.name;
    }
}
