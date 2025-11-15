package com.catowl.chatroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: ChatRoom
 * @description: 基础实体类，包含内部ID和外部ULID
 * @author: qqCatOwlbb
 * @create: 2025-11-15 13:42
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {
    private Long id;
    private String ulid;
}
