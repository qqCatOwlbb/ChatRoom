package com.catowl.chatroom.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: ChatRoom
 * @description: 处理 HttpOnly Cookie
 * @author: qqCatOwlbb
 * @create: 2025-11-15 16:46
 **/
public class CookieUtil {

    /**
     * 设置 HttpOnly Cookie
     * @param response HttpServletResponse
     * @param name Cookie 名称 (例如 "refresh_token")
     * @param value Cookie 值
     * @param maxAgeInSeconds 过期时间 (秒)
     */
    public static void setHttpOnlyCookie(HttpServletResponse response, String name, String value, long maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);       // 关键：设置为 HttpOnly
        cookie.setSecure(false);        // TODO: 在生产环境中应设为 true (仅限 HTTPS)
        cookie.setPath("/");            // 关键：路径设为根，确保所有请求都携带
        cookie.setMaxAge((int) maxAgeInSeconds);
        response.addCookie(cookie);
    }

    /**
     * 清除 Cookie
     * @param response HttpServletResponse
     * @param name Cookie 名称
     */
    public static void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null); // 值设为 null
        cookie.setHttpOnly(true);
        cookie.setSecure(false);        // TODO: 生产环境设为 true
        cookie.setPath("/");
        cookie.setMaxAge(0);            // 关键：设置立即过期
        response.addCookie(cookie);
    }
}
