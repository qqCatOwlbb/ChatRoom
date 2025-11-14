package com.catowl.chatroom.exception;

public interface BaseErrorInfoInterface {
    /**
    * @Description: 返回错误码
    * @Param:
    * @return: java.lang.String
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    String getResultCode();

    /** 
    * @Description: 返回错误消息
    * @Param: 
    * @return: java.lang.String
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    String getResultMsg();
}
