package com.videosite.controller;

import com.videosite.domain.entity.VideoNote;
import com.videosite.security.CustomUserDetailsService.CustomUserPrincipal;
import com.videosite.service.VideoNoteService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频笔记API控制器
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class VideoNoteController {
    
    private final VideoNoteService videoNoteService;
    
    /**
     * 添加笔记
     */
    @PostMapping
    public ResponseEntity<VideoNote> addNote(
            @RequestBody NoteRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        VideoNote note = videoNoteService.addNote(
            request.getVideoId(),
            principal.getUserId(),
            request.getTimestampSeconds(),
            request.getContent()
        );
        
        return ResponseEntity.ok(note);
    }
    
    /**
     * 获取视频的所有笔记
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<VideoNote>> getVideoNotes(@PathVariable Long videoId) {
        List<VideoNote> notes = videoNoteService.getVideoNotes(videoId);
        return ResponseEntity.ok(notes);
    }
    
    /**
     * 获取用户在某个视频上的笔记
     */
    @GetMapping("/video/{videoId}/my")
    public ResponseEntity<List<VideoNote>> getMyVideoNotes(
            @PathVariable Long videoId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<VideoNote> notes = videoNoteService.getUserVideoNotes(videoId, principal.getUserId());
        return ResponseEntity.ok(notes);
    }
    
    /**
     * 删除笔记
     */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        videoNoteService.deleteNote(noteId);
        return ResponseEntity.ok().build();
    }
    
    @Data
    public static class NoteRequest {
        private Long videoId;
        private Integer timestampSeconds;
        private String content;
    }
}
