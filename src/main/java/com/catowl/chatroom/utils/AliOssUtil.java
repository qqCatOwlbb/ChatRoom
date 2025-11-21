package com.catowl.chatroom.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * @program: ChatRoom
 * @description: 阿里云OSS配置类
 * @author: qqCatOwlbb
 * @create: 2025-11-21 11:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 上传文件 (默认私有权限，适用于待审核视频)
     *
     * @param inputStream 文件输入流 (MultipartFile.getInputStream())
     * @param objectName  OSS中的完整路径 (如: videos/2025/11/abc.mp4)
     * @return 文件的 ObjectKey (不是完整URL，因为私有文件URL直接访问不通)
     */
    public String upload(InputStream inputStream, String objectName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建元信息，指定上传为【私有】权限
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.Private);

            // 执行上传
            ossClient.putObject(bucketName, objectName, inputStream, metadata);

            log.info("OSS上传成功(私有): {}", objectName);

            // 返回 ObjectKey (例如 videos/abc.mp4)，方便后续生成签名
            return objectName;

        } catch (OSSException oe) {
            log.error("OSS服务端异常: RequestId={}, ErrorCode={}, Message={}",
                    oe.getRequestId(), oe.getErrorCode(), oe.getErrorMessage());
            throw new ServerException(ExceptionEnum.FILE_STORAGE_ERROR);
        } catch (ClientException ce) {
            log.error("OSS客户端连接异常: {}", ce.getMessage());
            throw new ServerException(ExceptionEnum.FILE_STORAGE_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取文件的完整访问 URL
     * * @param objectName 文件路径 (videos/abc.mp4)
     * @return 公共读 URL (https://bucket.endpoint/videos/abc.mp4)
     */
    public String getPublicUrl(String objectName) {
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    /**
     * 生成临时授权访问链接 (签名 URL)
     * 用于：用户预览自己的未审核视频、审核员后台查看视频
     *
     * @param objectName 文件路径
     * @return 带签名的长链接
     */
    public String getPrivateUrl(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 设置过期时间 (例如 1 小时)
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);

            // 生成签名 URL
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();

        } catch (Exception e) {
            log.error("OSS生成签名URL失败", e);
            throw new ServerException(ExceptionEnum.FILE_STORAGE_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 修改文件权限为【公共读】
     * 用于：视频审核通过后
     *
     * @param objectName 文件路径
     */
    public void makePublic(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 修改 ACL
            ossClient.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);
            log.info("OSS文件权限已修改为公共读: {}", objectName);
        } catch (Exception e) {
            log.error("OSS修改权限失败", e);
            throw new ServerException(ExceptionEnum.FILE_STORAGE_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }
}