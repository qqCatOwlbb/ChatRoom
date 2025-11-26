package com.catowl.chatroom.mapper;

import com.catowl.chatroom.model.DTO.request.LikeEventRequest;
import com.catowl.chatroom.model.VO.VideoFeedVO;
import com.catowl.chatroom.model.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @program: ChatRoom
 * @description: 视频
 * @author: qqCatOwlbb
 * @create: 2025-11-20 22:24
 **/
@Mapper
public interface VideoMapper {

    /**
     * 插入视频
     */
    int insertVideo(Video video);

    /**
     * 根据 ID 查找 (用于内部逻辑，如审核、删除)
     */
    Video findById(@Param("id") Long id);

    /**
     * 根据 ULID 查找 (用于对外接口)
     */
    Video findByUlid(@Param("ulid") String ulid);

    /**
     * 批量根据 ID 查询 (用于热门榜单、Redis回源)
     */
    List<Video> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 游标分页查询视频流 (去 Join 化，支持搜索)
     * @param cursor 上一页最后一条视频的 ULID
     * @param limit 条数
     * @param title 搜索标题 (可选)
     */
    List<Video> findFeedList(@Param("cursor") String cursor,
                             @Param("limit") int limit,
                             @Param("title") String title);

    /**
     * 更新视频信息 (动态 SQL)
     */
    int updateVideo(Video video);

    /**
     * 增加播放量 (原子更新)
     */
    int incrementViewCount(@Param("id") Long id, @Param("increment") int increment);

    /**
     * 增加点赞量 (原子更新)
     */
    int incrementLikeCount(@Param("id") Long id, @Param("increment") int increment);

    /**
     * 减少点赞量 (原子更新)
     */
    int decrementLikeCount(@Param("id") Long id, @Param("decrement") int decrement);

    /**
     * 删除视频
     */
    int deleteById(@Param("id") Long id);
    
    List<VideoFeedVO> selectFeedVOByIds(@Param("ids") List<Long> ids);

    void batchUpdateScores(List<Video> videos);

    void batchInsert(@Param("list")List<LikeEventRequest> list);

    void batchDelete(@Param("list")List<LikeEventRequest> list);
}