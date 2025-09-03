package com.videosite.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video")
@EntityListeners(AuditingEntityListener.class)
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "keywords", length = 1000)
    private String keywords;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;
    
    @Column(name = "thumb_path", nullable = false, length = 500)
    private String thumbPath;
    
    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;
    
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;
    
    @Column(name = "views_total", nullable = false)
    private Long viewsTotal = 0L;
    
    @Column(name = "downloads_total", nullable = false)
    private Long downloadsTotal = 0L;
    
    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility = "PUBLIC";
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // 关联查询上传者信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", insertable = false, updatable = false)
    private User uploader;
}
