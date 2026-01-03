package com.videosite.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 视频笔记实体类 - 用于实现基于时间锚点的交互式学习功能
 */
@Data
@Entity
@Table(name = "video_note", indexes = {
    @Index(name = "idx_video_id", columnList = "video_id"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
public class VideoNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "timestamp_seconds", nullable = false)
    private Integer timestampSeconds;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
