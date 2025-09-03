package com.videosite.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "video_daily_stats",
       uniqueConstraints = @UniqueConstraint(name = "uk_video_daily_stats", columnNames = {"video_id", "stat_date"}))
public class VideoDailyStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;
    
    @Column(name = "views", nullable = false)
    private Integer views = 0;
    
    @Column(name = "downloads", nullable = false)
    private Integer downloads = 0;
    
    // 关联视频信息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", insertable = false, updatable = false)
    private Video video;
}
