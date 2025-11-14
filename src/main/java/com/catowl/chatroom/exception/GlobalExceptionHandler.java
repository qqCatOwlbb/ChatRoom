package com.catowl.chatroom.exception;

import com.catowl.chatroom.Model.response.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: ChatRoom
 * @description: 全局异常处理器
 * @author: qqCatOwlbb
 * @create: 2025-11-14 15:39
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
    * @Description: 处理未认证异常
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    @ExceptionHandler(value = UnauthorizedException.class)
    public ResultResponse<String> UnauthorizedExceptionHandler(HttpServletRequest req, UnauthorizedException e){
        logger.error("用户未认证！具体错误错误信息：{}",e.getErrorMsg());
        return ResultResponse.error(e.getErrorCode(),e.getMessage());
    }

    /**
    * @Description: 处理空指针异常
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    @ExceptionHandler(value =NullPointerException.class)
    public ResultResponse exceptionHandler(HttpServletRequest req, NullPointerException e){
        logger.error("发生空指针异常！原因是:",e);
        return ResultResponse.error(ExceptionEnum.BODY_NOT_MATCH);
    }

    /**
    * @Description: 处理其它异常
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    @ExceptionHandler(value =Exception.class)
    public ResultResponse exceptionHandler(HttpServletRequest req, Exception e){
        logger.error("未知异常！原因是:",e);
        return ResultResponse.error(ExceptionEnum.INTERNAL_SERVER_ERROR);
    }
}
