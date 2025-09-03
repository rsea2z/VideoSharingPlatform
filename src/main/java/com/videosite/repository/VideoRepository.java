package com.videosite.repository;

import com.videosite.domain.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    /**
     * 分页查询所有视频，按创建时间倒序
     */
    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据上传者ID查询视频
     */
    Page<Video> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);
    
    /**
     * 搜索视频（标题、关键词、上传者用户名）
     */
    @Query("""
        SELECT DISTINCT v FROM Video v 
        LEFT JOIN v.uploader u
        WHERE (:keywords IS NULL OR :keywords = '' OR
               LOWER(v.title) LIKE LOWER(CONCAT('%', :keywords, '%')) OR
               LOWER(v.keywords) LIKE LOWER(CONCAT('%', :keywords, '%')) OR
               LOWER(u.username) LIKE LOWER(CONCAT('%', :keywords, '%')))
        ORDER BY v.createdAt DESC
        """)
    Page<Video> searchVideos(@Param("keywords") String keywords, Pageable pageable);
    
    /**
     * 复杂搜索：AND和OR逻辑
     */
    @Query(value = """
        SELECT v.* FROM video v 
        LEFT JOIN user u ON v.uploader_id = u.id
        WHERE (:andKeywords IS NULL OR (
            SELECT COUNT(DISTINCT token) FROM (
                SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(:andKeywords, ',', numbers.n), ',', -1)) as token
                FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
                      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) numbers
                WHERE CHAR_LENGTH(:andKeywords) - CHAR_LENGTH(REPLACE(:andKeywords, ',', '')) >= numbers.n - 1
            ) tokens
            WHERE token != '' AND (
                LOWER(v.title) LIKE LOWER(CONCAT('%', token, '%')) OR
                LOWER(v.keywords) LIKE LOWER(CONCAT('%', token, '%')) OR
                LOWER(u.username) LIKE LOWER(CONCAT('%', token, '%'))
            )
        ) = :tokenCount)
        ORDER BY v.created_at DESC
        """, nativeQuery = true)
    Page<Video> searchVideosWithAndLogic(@Param("andKeywords") String andKeywords, 
                                        @Param("tokenCount") int tokenCount, 
                                        Pageable pageable);
    
    /**
     * 获取播放量Top10视频（包含上传者信息）
     */
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.uploader ORDER BY v.viewsTotal DESC")
    List<Video> findTop10ByOrderByViewsTotalDesc();
    
    /**
     * 获取下载量Top10视频（包含上传者信息）
     */
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.uploader ORDER BY v.downloadsTotal DESC")
    List<Video> findTop10ByOrderByDownloadsTotalDesc();
    
    /**
     * 原子性增加播放次数
     */
    @Modifying
    @Query("UPDATE Video v SET v.viewsTotal = v.viewsTotal + 1 WHERE v.id = :videoId")
    int incrementViewsTotal(@Param("videoId") Long videoId);
    
    /**
     * 原子性增加下载次数
     */
    @Modifying
    @Query("UPDATE Video v SET v.downloadsTotal = v.downloadsTotal + 1 WHERE v.id = :videoId")
    int incrementDownloadsTotal(@Param("videoId") Long videoId);
    
    /**
     * 获取总播放次数
     */
    @Query("SELECT SUM(v.viewsTotal) FROM Video v")
    Long getTotalViews();
    
    /**
     * 获取总下载次数
     */
    @Query("SELECT SUM(v.downloadsTotal) FROM Video v")
    Long getTotalDownloads();
    
    /**
     * 查询所有视频，按创建时间倒序（无分页）
     */
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.uploader ORDER BY v.createdAt DESC")
    List<Video> findAllByOrderByCreatedAtDesc();
}
