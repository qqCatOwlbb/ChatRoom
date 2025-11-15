package com.catowl.chatroom.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: ChatRoom
 * @description: 权限异常
 * @author: qqCatOwlbb
 * @create: 2025-11-15 10:58
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ForbiddenException extends RuntimeException {

    private final BaseErrorInfoInterface errorInfo;

    public ForbiddenException(BaseErrorInfoInterface errorInfo){
        super(errorInfo.getResultMsg());
        this.errorInfo = errorInfo;
    }

    public ForbiddenException(BaseErrorInfoInterface errorInfo, String customMessage){
        super(customMessage);
        this.errorInfo = errorInfo;
    }
}
