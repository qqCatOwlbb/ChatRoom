package com.catowl.chatroom.service;

import java.util.List;
import java.util.Set;

/**
 * @program: ChatRoom
 * @description: 视频：榜单的计算，数据分析
 * @author: qqCatOwlbb
 * @create: 2025-11-22 09:57
 **/
public interface HotVideoService {
    /** 
    * @Description: 批量更新视频热度
    * @Param: [videoIds]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    void updateHotScoresBatch(Set<Long> videoIds);
    /** 
    * @Description: 获取热门榜单 Top N 的视频 ID
    * @Param: [limits]
    * @return: java.util.List<java.lang.Long>
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    List<Long> getTopHotVideoIds(int limits);
    
    /** 
    * @Description: 从榜单中移除视频
    * @Param: [videoId]
    * @return: void
    * @Author: qqCatOwlbb
    * @Date: 2025/11/22
    */
    void removeVideoFromRank(Long videoId);
}
