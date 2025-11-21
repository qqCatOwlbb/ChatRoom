package com.catowl.chatroom.config;

import com.catowl.chatroom.utils.AliOssUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: ChatRoom
 * @description: 阿里云OSS配置类
 * @author: qqCatOwlbb
 * @create: 2025-11-21 12:07
 **/
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss") // 自动读取 aliyun.oss 开头的配置
@Data
public class OssConfig {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public AliOssUtil aliOssUtil() {
        return new AliOssUtil(endpoint, accessKeyId, accessKeySecret, bucketName);
    }
}
