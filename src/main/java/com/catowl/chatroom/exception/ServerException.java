package com.catowl.chatroom.exception;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: ChatRoom
 * @description: 服务内部异常
 * @author: qqCatOwlbb
 * @create: 2025-11-15 11:13
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class ServerException extends RuntimeException {
    private final BaseErrorInfoInterface errorInfo;

    public ServerException(BaseErrorInfoInterface errorInfo){
        super(errorInfo.getResultMsg());
        this.errorInfo = errorInfo;
    }

    public ServerException(BaseErrorInfoInterface errorInfo, String customMessage){
        super(customMessage);
        this.errorInfo = errorInfo;
    }
}
