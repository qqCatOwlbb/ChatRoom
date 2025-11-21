package com.catowl.chatroom.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @program: ChatRoom
 * @description: 处理文件
 * @author: qqCatOwlbb
 * @create: 2025-11-20 21:54
 **/
public interface FileService {
    /** 
    * @Description: 上传文件
    * @Param: [file, subFolder]
    * @return: java.lang.String
    * @Author: qqCatOwlbb
    * @Date: 2025/11/20
    */
    String upload(MultipartFile file, String subFolder);
}
