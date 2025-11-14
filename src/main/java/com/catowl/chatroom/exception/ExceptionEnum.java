package com.catowl.chatroom.exception;

import com.fasterxml.jackson.databind.ser.Serializers;

import javax.validation.Valid;

/**
 * @program: ChatRoom
 * @description: 异常处理枚举类
 * @author: qqCatOwlbb
 * @create: 2025-11-14 15:04
 **/
public enum ExceptionEnum implements BaseErrorInfoInterface {

    /**
     * 第一位： 2 代表成功，4 代表客户端错误，5 代表服务端错误。
     * 第二位： 0 代表通用错误，1 代表用户/账户，2 代表安全/权限，3 代表视频，4 代表 IM 通信。
     * 后两位： 代表具体错误。
     */

    // 通用错误码
    SUCCESS("2000", "成功！"),
    BODY_NOT_MATCH("4000", "请求的数据格式不符！"),
    SIGNATURE_NOT_MATCH("4001", "请求的数字签名不匹配!"),
    VALIDATION_ERROR("4002", "参数校验失败！"),    // (用于 DTO 的 @Valid 校验)
    NOT_FOUND("4004", "未找到该资源!"),   // (通用 404)
    REQUEST_METHOD_NOT_SUPPORTED("4005", "不支持的请求方法！"),
    INTERNAL_SERVER_ERROR("5000", "服务器内部错误!"),
    SERVER_BUSY("5003", "服务器正忙，请稍后再试!"),
    DATABASE_ERROR("5011", "数据库服务异常！"),
    REDIS_ERROR("5012", "缓存服务异常！"),
    MQ_ERROR("5013", "消息队列服务异常！"),
    IDEMPOTENT_REJECT("5014", "请求重复，已被处理！"),

    // 与用户注册、登录、信息相关的错误码
    USERNAME_EXISTS("4101", "用户名已存在！"),
    USERNAME_INVALID("4102", "用户名格式不合法！"),
    PASSWORD_INVALID("4103", "密码格式不合法！"),
    USER_NOT_FOUND("4104", "用户不存在！"),
    PASSWORD_WRONG("4105", "用户名或密码错误！"),
    USER_ACCOUNT_DISABLED("4106", "账户已被禁用！"),

    // 与 Spring Security、JWT、Netty 鉴权、角色权限相关的错误码
    TOKEN_NOT_PROVIDED("4201", "未提供身份凭证！"),
    TOKEN_INVALID("4202", "身份凭证无效或已过期！"),
    TOKEN_EXPIRED("4203", "身份凭证已过期，请重新登录！"),
    UNAUTHORIZED("4210", "未经授权！"),  // (通常指未登录)
    FORBIDDEN("4211", "权限不足，禁止访问！"),    // (通常指已登录，但角色不够)
    WEBSOCKET_AUTH_FAILED("4212", "WebSocket 连接鉴权失败！"), // (用于 Netty 握手阶段)

    // 与视频上传、审核、观看相关的错误
    FILE_EMPTY("4301", "上传的文件不能为空！"),
    FILE_TYPE_NOT_SUPPORTED("4302", "不支持的文件类型！"),
    FILE_SIZE_EXCEEDED("4303", "文件大小超出限制！"),
    VIDEO_NOT_FOUND("4310", "视频不存在！"),
    VIDEO_NOT_PENDING_AUDIT("4311", "该视频不在待审核状态！"),
    USER_NOT_AUDITOR("4312", "当前用户不是审核员！"),
    VIDEO_NOT_APPROVED("4320", "该视频尚未通过审核，无法观看！"),
    FILE_STORAGE_ERROR("5301", "文件存储服务异常！"),

    // 与 WebSocket 连接、聊天室业务逻辑相关的错误码
    WEBSOCKET_HANDSHAKE_FAILED("4401", "WebSocket 握手失败！"),
    MESSAGE_FORMAT_INVALID("4410", "消息体格式错误！"),
    MESSAGE_TYPE_NOT_SUPPORTED("4411", "不支持的消息类型！"),
    CONTACT_ALREADY_EXISTS("4421", "对方已经是你的好友！"),
    CANNOT_ADD_SELF("4422", "不能添加自己为好友！"),
    USER_IS_BLOCKED("4423", "你已被对方拉黑！"),
    NOT_FRIENDS("4424", "对方还不是你的好友！"),
    CONVERSATION_NOT_FOUND("4430", "会话不存在！"),
    USER_NOT_IN_GROUP("4431", "你不在该群聊中！"),
    MESSAGE_CONTAINS_SENSITIVE_WORDS("4440", "消息包含敏感词！"),
    MESSAGE_REJECTED_BY_POLICY("4441", "消息发送被拒绝（例如：非好友限制）！"),
    MESSAGE_TARGET_NOT_ONLINE("4442", "对方不在线！"),    // (如果业务需要，可以返回此码，但通常 IM 会走离线)
    MESSAGE_SEQUENCE_ERROR("5401", "消息时序生成异常！");

    private final String resultCode;

    private final String resultMsg;

    ExceptionEnum(String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public String getResultMsg() {
        return resultMsg;
    }
}
