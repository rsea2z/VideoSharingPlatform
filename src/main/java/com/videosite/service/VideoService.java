package com.videosite.service;

import com.videosite.domain.entity.Video;
import com.videosite.dto.form.SearchQuery;
import com.videosite.dto.form.UploadForm;
import com.videosite.repository.VideoRepository;
import com.videosite.util.KeywordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {
    
    private final VideoRepository videoRepository;
    private final StorageService storageService;
    private final TranscodeService transcodeService;
    private final StatsService statsService;
    
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB
    
    /**
     * 分页查询所有视频
     */
    public Page<Video> findAllVideos(Pageable pageable) {
        return videoRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 根据ID查找视频
     */
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }
    
    /**
     * 搜索视频
     */
    public Page<Video> searchVideos(SearchQuery query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        
        if (!query.hasQuery()) {
            return videoRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        // 实现复杂搜索逻辑
        String cleanQuery = query.getCleanQuery().toLowerCase();
        List<String> keywords = KeywordUtils.parseKeywords(cleanQuery);
        
        if (keywords.isEmpty()) {
            return videoRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        // 简化版搜索，直接使用关键词匹配
        return videoRepository.searchVideos(cleanQuery, pageable);
    }
    
    /**
     * 上传视频
     */
    @Transactional
    public Video uploadVideo(UploadForm form, Long uploaderId) throws IOException, InterruptedException {
        MultipartFile file = form.getVideoFile();
        
        // 验证文件
        validateUploadFile(file);
        
        // 生成存储路径
        String videoStoragePath = storageService.generateVideoStoragePath();
        String thumbnailStoragePath = storageService.generateThumbnailStoragePath();
        
        // 确保目录存在
        storageService.ensureDirectoryExists(videoStoragePath);
        storageService.ensureDirectoryExists(thumbnailStoragePath);
        
        // 获取完整路径
        Path videoPath = storageService.getFullPath(videoStoragePath);
        Path thumbnailPath = storageService.getFullPath(thumbnailStoragePath);
        
        try {
            // 保存视频文件
            Files.copy(file.getInputStream(), videoPath);
            log.info("视频文件保存成功: {}", videoPath);
            
            // 验证视频格式并获取信息
            TranscodeService.VideoInfo videoInfo = transcodeService.validateVideoFile(videoPath);
            if (!videoInfo.isValidFormat()) {
                throw new IllegalArgumentException("视频格式不符合要求，必须是H.264视频编码和AAC音频编码的MP4文件");
            }
            
            // 生成缩略图
            transcodeService.generateThumbnail(videoPath, thumbnailPath, videoInfo.getDurationSeconds());
            
            // 处理关键词
            String processedKeywords = KeywordUtils.normalizeKeywords(form.getKeywords());
            
            // 创建视频记录
            Video video = new Video();
            video.setTitle(form.getTitle());
            video.setDescription(form.getDescription());
            video.setKeywords(processedKeywords);
            video.setOriginalFilename(file.getOriginalFilename());
            video.setStoragePath(videoStoragePath);
            video.setThumbPath(thumbnailStoragePath);
            video.setUploaderId(uploaderId);
            video.setSizeBytes(file.getSize());
            video.setDurationSeconds(videoInfo.getDurationSeconds());
            
            Video savedVideo = videoRepository.save(video);
            log.info("视频上传成功: ID={}, 标题={}", savedVideo.getId(), savedVideo.getTitle());
            
            return savedVideo;
            
        } catch (Exception e) {
            // 清理已保存的文件
            storageService.deleteFile(videoStoragePath);
            storageService.deleteFile(thumbnailStoragePath);
            throw e;
        }
    }
    
    /**
     * 删除视频
     */
    @Transactional
    public void deleteVideo(Long videoId, Long currentUserId, boolean isAdmin) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("视频不存在"));
        
        // 权限检查
        if (!isAdmin && !video.getUploaderId().equals(currentUserId)) {
            throw new SecurityException("没有权限删除此视频");
        }
        
        // 删除物理文件
        storageService.deleteFile(video.getStoragePath());
        storageService.deleteFile(video.getThumbPath());
        
        // 删除数据库记录（级联删除统计数据）
        videoRepository.delete(video);
        
        log.info("视频删除成功: ID={}, 标题={}", videoId, video.getTitle());
    }
    
    /**
     * 记录播放
     */
    @Transactional
    public void recordView(Long videoId) {
        // 增加总播放次数
        int updated = videoRepository.incrementViewsTotal(videoId);
        if (updated > 0) {
            // 记录日统计
            statsService.recordDailyView(videoId, LocalDate.now());
            log.debug("记录播放: videoId={}", videoId);
        }
    }
    
    /**
     * 记录下载
     */
    @Transactional
    public void recordDownload(Long videoId) {
        // 增加总下载次数
        int updated = videoRepository.incrementDownloadsTotal(videoId);
        if (updated > 0) {
            // 记录日统计
            statsService.recordDailyDownload(videoId, LocalDate.now());
            log.debug("记录下载: videoId={}", videoId);
        }
    }
    
    /**
     * 获取播放量Top10
     */
    public List<Video> getTopViewedVideos() {
        return videoRepository.findTop10ByOrderByViewsTotalDesc();
    }
    
    /**
     * 获取下载量Top10
     */
    public List<Video> getTopDownloadedVideos() {
        return videoRepository.findTop10ByOrderByDownloadsTotalDesc();
    }
    
    /**
     * 获取总视频数
     */
    public Long getTotalVideoCount() {
        return videoRepository.count();
    }
    
    /**
     * 获取所有视频
     */
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * 验证上传文件
     */
    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的视频文件");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许200MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp4")) {
            throw new IllegalArgumentException("只支持MP4格式的视频文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("文件类型不正确，请上传视频文件");
        }
    }
}
