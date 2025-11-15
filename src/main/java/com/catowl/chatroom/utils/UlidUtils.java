package com.catowl.chatroom.utils;

import de.huxhorn.sulky.ulid.ULID;

/**
 * @program: ChatRoom
 * @description: Ulid工具类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 13:17
 **/
public class UlidUtils {

    private static final ULID ulid = new ULID();

    // 生成ULID字符串（26位）
    public static String generate() {
        return ulid.nextULID();
    }

    // String -> byte[]
    public static byte[] toBytes(String ulidString) {
        ULID.Value value = ulid.parseULID(ulidString);
        return value.toBytes();
    }

    // byte[] -> String
    public static String fromBytes(byte[] bytes) {
        if (bytes == null) return null;
        ULID.Value value = ULID.fromBytes(bytes);
        return value.toString();
    }
}
