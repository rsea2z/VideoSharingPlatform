package com.videosite.repository;

import com.videosite.domain.entity.VideoDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoDailyStatsRepository extends JpaRepository<VideoDailyStats, Long> {
    
    /**
     * 根据视频ID和日期查找统计记录
     */
    Optional<VideoDailyStats> findByVideoIdAndStatDate(Long videoId, LocalDate statDate);
    
    /**
     * 获取指定日期范围内的统计数据
     */
    @Query("SELECT vds FROM VideoDailyStats vds WHERE vds.statDate BETWEEN :startDate AND :endDate ORDER BY vds.statDate")
    List<VideoDailyStats> findByStatDateBetweenOrderByStatDate(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    /**
     * 获取每日播放量统计（按日期聚合）
     */
    @Query("SELECT vds.statDate, SUM(vds.views) FROM VideoDailyStats vds " +
           "WHERE vds.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY vds.statDate ORDER BY vds.statDate")
    List<Object[]> getDailyViewsStats(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 获取每日下载量统计（按日期聚合）
     */
    @Query("SELECT vds.statDate, SUM(vds.downloads) FROM VideoDailyStats vds " +
           "WHERE vds.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY vds.statDate ORDER BY vds.statDate")
    List<Object[]> getDailyDownloadsStats(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * 更新或插入播放次数统计
     */
    @Modifying
    @Query(value = "INSERT INTO video_daily_stats (video_id, stat_date, views, downloads) " +
                   "VALUES (:videoId, :statDate, 1, 0) " +
                   "ON DUPLICATE KEY UPDATE views = views + 1", nativeQuery = true)
    int upsertViewsStats(@Param("videoId") Long videoId, @Param("statDate") LocalDate statDate);
    
    /**
     * 更新或插入下载次数统计
     */
    @Modifying
    @Query(value = "INSERT INTO video_daily_stats (video_id, stat_date, views, downloads) " +
                   "VALUES (:videoId, :statDate, 0, 1) " +
                   "ON DUPLICATE KEY UPDATE downloads = downloads + 1", nativeQuery = true)
    int upsertDownloadsStats(@Param("videoId") Long videoId, @Param("statDate") LocalDate statDate);
}
