package com.catowl.chatroom.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 未认证异常401
 * @author: qqCatOwlbb
 * @create: 2025-11-14 15:16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnauthorizedException extends RuntimeException {
    private static final long seriaVersionUID = 1L;

    protected String errorCode;

    protected String errorMsg;

    public UnauthorizedException(String message) {
        super(message);
    }
}
