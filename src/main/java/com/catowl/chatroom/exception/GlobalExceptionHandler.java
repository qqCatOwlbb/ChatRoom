package com.catowl.chatroom.exception;

import com.catowl.chatroom.model.DTO.response.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    * @Description: 处理服务内部异常 5xxx
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    @ExceptionHandler(value = ServerException.class)
    public ResultResponse<String> ServerExceptionHandler(HttpServletRequest req, ServerException e){
        logger.error("服务内部异常，{}", e.getErrorInfo().getResultMsg());
        return ResultResponse.error(e.getErrorInfo());
    }

    /**
    * @Description: 处理资源未找到异常 404 / 4004 / 4310
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    @ExceptionHandler(value = NotFoundException.class)
    public ResultResponse<String> NotFoundExceptionHandler(HttpServletRequest req, NotFoundException e){
        logger.error("资源未找到：{}", e.getErrorInfo().getResultMsg());
        return ResultResponse.error(e.getErrorInfo());
    }

    /** 
    * @Description: 处理权限认证异常 403 / 4211
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    @ExceptionHandler(value = ForbiddenException.class)
    public ResultResponse<String> ForbiddenExceptionHandler(HttpServletRequest req, ForbiddenException e){
        logger.error("权限认证异常：{}", e.getErrorInfo().getResultMsg());
        return ResultResponse.error(e.getErrorInfo());
    }
    
    /** 
    * @Description: 处理业务逻辑异常 （例如：用户名已存在 4101、视频格式不支持 4302、不能添加自己为好友 4422 等）
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/15
    */
    @ExceptionHandler(value = BusinessException.class)
    public ResultResponse<String> BusinessExceptionHandler(HttpServletRequest req, BusinessException e){
        logger.error("业务逻辑异常：{}", e.getErrorInfo().getResultMsg());
        return ResultResponse.error(e.getErrorInfo());
    }

    /**
    * @Description: 处理未认证异常 401 / 42xx
    * @Param: [req, e]
    * @return: com.catowl.chatroom.Model.response.ResultResponse<java.lang.String>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/14
    */
    @ExceptionHandler(value = UnauthorizedException.class)
    public ResultResponse<String> UnauthorizedExceptionHandler(HttpServletRequest req, UnauthorizedException e){
        logger.error("用户未认证！错误信息：{}",e.getErrorInfo().getResultMsg());
        return ResultResponse.error(e.getErrorInfo());
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
