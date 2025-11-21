package com.catowl.chatroom.model.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @program: ChatRoom
 * @description: 修改视频信息
 * @author: qqCatOwlbb
 * @create: 2025-11-21 13:26
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoUpdateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题过长")
    private String title;

    @Size(max = 200)
    private String description;
}
