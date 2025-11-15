package com.catowl.chatroom.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.aspectj.weaver.ast.Not;

/**
 * @program: ChatRoom
 * @description: 资源未找到
 * @author: qqCatOwlbb
 * @create: 2025-11-15 11:07
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class NotFoundException extends RuntimeException {

    private final BaseErrorInfoInterface errorInfo;

    public NotFoundException(BaseErrorInfoInterface errorInfo){
        super(errorInfo.getResultMsg());
        this.errorInfo = errorInfo;
    }

    public NotFoundException(BaseErrorInfoInterface errorInfo, String customMessage){
        super(customMessage);
        this.errorInfo = errorInfo;
    }
}
