package com.videosite.service;

import com.videosite.domain.entity.VideoDailyStats;
import com.videosite.repository.VideoDailyStatsRepository;
import com.videosite.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    
    private final VideoDailyStatsRepository statsRepository;
    private final VideoRepository videoRepository;
    
    /**
     * 记录每日播放统计
     */
    @Transactional
    public void recordDailyView(Long videoId, LocalDate date) {
        try {
            statsRepository.upsertViewsStats(videoId, date);
        } catch (Exception e) {
            log.error("记录播放统计失败: videoId={}, date={}", videoId, date, e);
        }
    }
    
    /**
     * 记录每日下载统计
     */
    @Transactional
    public void recordDailyDownload(Long videoId, LocalDate date) {
        try {
            statsRepository.upsertDownloadsStats(videoId, date);
        } catch (Exception e) {
            log.error("记录下载统计失败: videoId={}, date={}", videoId, date, e);
        }
    }
    
    /**
     * 获取指定日期范围的统计数据
     */
    public List<VideoDailyStats> getStatsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return statsRepository.findByStatDateBetweenOrderByStatDate(startDate, endDate);
    }
    
    /**
     * 获取每日播放量统计
     */
    public List<Object[]> getDailyViewsStats(LocalDate startDate, LocalDate endDate) {
        return statsRepository.getDailyViewsStats(startDate, endDate);
    }
    
    /**
     * 获取每日下载量统计
     */
    public List<Object[]> getDailyDownloadsStats(LocalDate startDate, LocalDate endDate) {
        return statsRepository.getDailyDownloadsStats(startDate, endDate);
    }
    
    /**
     * 获取指定视频在指定日期的统计信息
     */
    public Optional<VideoDailyStats> getVideoStats(Long videoId, LocalDate date) {
        return statsRepository.findByVideoIdAndStatDate(videoId, date);
    }
    
    /**
     * 获取总播放次数
     */
    public Long getTotalViews() {
        Long total = videoRepository.getTotalViews();
        return total != null ? total : 0L;
    }
    
    /**
     * 获取总下载次数
     */
    public Long getTotalDownloads() {
        Long total = videoRepository.getTotalDownloads();
        return total != null ? total : 0L;
    }
}
