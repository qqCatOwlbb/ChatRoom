package com.catowl.chatroom.Model.response;

import com.alibaba.fastjson.JSONObject;
import com.catowl.chatroom.exception.BaseErrorInfoInterface;
import com.catowl.chatroom.exception.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChatRoom
 * @description: 自定义数据传输
 * @author: qqCatOwlbb
 * @create: 2025-11-14 15:22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse<T> {

    private String code;
    private String message;
    private T result;

    public ResultResponse(BaseErrorInfoInterface errorInfo) {
        this.code = errorInfo.getResultCode();
        this.message = errorInfo.getResultMsg();
    }

    /**
     * 成功（无数据）
     */
    public static <T> ResultResponse<T> success() {
        return success(null);
    }

    /**
     * 成功（有数据）
     */
    public static <T> ResultResponse<T> success(T data) {
        ResultResponse<T> rb = new ResultResponse<>();
        rb.setCode(ExceptionEnum.SUCCESS.getResultCode());
        rb.setMessage(ExceptionEnum.SUCCESS.getResultMsg());
        rb.setResult(data);
        return rb;
    }

    /**
     * 失败（枚举）
     */
    public static <T> ResultResponse<T> error(BaseErrorInfoInterface errorInfo) {
        ResultResponse<T> rb = new ResultResponse<>();
        rb.setCode(errorInfo.getResultCode());
        rb.setMessage(errorInfo.getResultMsg());
        rb.setResult(null);
        return rb;
    }

    /**
     * 失败（指定 code, message）
     */
    public static <T> ResultResponse<T> error(String code, String message) {
        ResultResponse<T> rb = new ResultResponse<>();
        rb.setCode(code);
        rb.setMessage(message);
        rb.setResult(null);
        return rb;
    }

    /**
     * 失败（指定 message）
     */
    public static <T> ResultResponse<T> error(String message) {
        ResultResponse<T> rb = new ResultResponse<>();
        rb.setCode("-1");
        rb.setMessage(message);
        rb.setResult(null);
        return rb;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}

