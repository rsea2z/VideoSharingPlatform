package com.videosite.service;

import com.videosite.domain.entity.VideoNote;
import com.videosite.repository.VideoNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 视频笔记服务 - 实现交互式学习功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoNoteService {
    
    private final VideoNoteRepository videoNoteRepository;
    
    /**
     * 添加笔记
     */
    @Transactional
    public VideoNote addNote(Long videoId, Long userId, Integer timestampSeconds, String content) {
        VideoNote note = new VideoNote();
        note.setVideoId(videoId);
        note.setUserId(userId);
        note.setTimestampSeconds(timestampSeconds);
        note.setContent(content);
        
        VideoNote savedNote = videoNoteRepository.save(note);
        log.info("用户 {} 在视频 {} 的 {}s 处添加了笔记", userId, videoId, timestampSeconds);
        
        return savedNote;
    }
    
    /**
     * 获取视频的所有笔记（按时间戳排序）
     */
    public List<VideoNote> getVideoNotes(Long videoId) {
        return videoNoteRepository.findByVideoIdOrderByTimestampSecondsAsc(videoId);
    }
    
    /**
     * 获取用户在某个视频上的所有笔记
     */
    public List<VideoNote> getUserVideoNotes(Long videoId, Long userId) {
        return videoNoteRepository.findByVideoIdAndUserIdOrderByTimestampSecondsAsc(videoId, userId);
    }
    
    /**
     * 删除笔记
     */
    @Transactional
    public void deleteNote(Long noteId) {
        videoNoteRepository.deleteById(noteId);
        log.info("删除笔记: {}", noteId);
    }
    
    /**
     * 清空视频的所有笔记（当视频被删除时调用）
     */
    @Transactional
    public void deleteVideoNotes(Long videoId) {
        videoNoteRepository.deleteByVideoId(videoId);
        log.info("清空视频 {} 的所有笔记", videoId);
    }
}
