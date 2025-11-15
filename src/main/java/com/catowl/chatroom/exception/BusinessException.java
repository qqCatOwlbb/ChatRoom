package com.catowl.chatroom.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 业务异常
 * @author: qqCatOwlbb
 * @create: 2025-11-15 10:47
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {
    private final BaseErrorInfoInterface errorInfo;

    public BusinessException(BaseErrorInfoInterface errorInfo){
        super(errorInfo.getResultMsg());
        this.errorInfo = errorInfo;
    }

    public BusinessException(BaseErrorInfoInterface errorInfo, String customMessage){
        super(customMessage);
        this.errorInfo = errorInfo;
    }
}
