package com.videosite.repository;

import com.videosite.domain.entity.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视频笔记Repository
 */
@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    
    /**
     * 查询某个视频的所有笔记（按时间戳排序）
     */
    List<VideoNote> findByVideoIdOrderByTimestampSecondsAsc(Long videoId);
    
    /**
     * 查询某个用户对某个视频的所有笔记
     */
    List<VideoNote> findByVideoIdAndUserIdOrderByTimestampSecondsAsc(Long videoId, Long userId);
    
    /**
     * 删除某个视频的所有笔记
     */
    void deleteByVideoId(Long videoId);
}
