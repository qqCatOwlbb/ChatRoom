package com.catowl.chatroom.service.impl;

import com.catowl.chatroom.exception.BusinessException;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.service.FileService;
import com.catowl.chatroom.utils.UlidUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/**
 * @program: ChatRoom
 * @description: 本地文件处理
 * @author: qqCatOwlbb
 * @create: 2025-11-20 21:55
 **/
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String rootPath;

    @Value("${file.access-path}")
    private String accessPathPrefix;

    @Override
    public String upload(MultipartFile file, String subFolder) {
        if(file.isEmpty()){
            throw new BusinessException(ExceptionEnum.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(!suffix.matches("(?i)^.(emp4|avi|mov|jpg|png|jpeg)")){
            throw new BusinessException(ExceptionEnum.FILE_TYPE_NOT_SUPPORTED);
        }

        String fileName = UlidUtils.generate() + suffix;
        String dataPath = LocalDate.now().toString();
        String relativePath = subFolder + File.separator +dataPath;

        File destDir = new File(rootPath + File.separator + relativePath);
        if(!destDir.exists()){
            destDir.mkdir();
        }
        File destFile = new File(destDir,fileName);

        try{
            file.transferTo(destFile);
        }catch (IOException e){
            throw new BusinessException(ExceptionEnum.FILE_STORAGE_ERROR);
        }
        return accessPathPrefix + subFolder + "/" + dataPath + "/" + fileName;
    }
}
