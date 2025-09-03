package com.videosite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    
    @Value("${app.storage.base-dir:./data}")
    private String baseDir;
    
    @Value("${app.storage.video-dir:videos}")
    private String videoDir;
    
    @Value("${app.storage.thumb-dir:thumbs}")
    private String thumbDir;
    
    /**
     * 初始化存储目录
     */
    public void initializeStorageDirectories() throws IOException {
        Path basePath = Paths.get(baseDir);
        Path videoPath = basePath.resolve(videoDir);
        Path thumbPath = basePath.resolve(thumbDir);
        
        Files.createDirectories(basePath);
        Files.createDirectories(videoPath);
        Files.createDirectories(thumbPath);
        
        log.info("存储目录初始化完成: {}", basePath.toAbsolutePath());
    }
    
    /**
     * 生成视频存储路径
     */
    public String generateVideoStoragePath() {
        LocalDate now = LocalDate.now();
        String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID().toString() + ".mp4";
        return videoDir + "/" + dateDir + "/" + filename;
    }
    
    /**
     * 生成缩略图存储路径
     */
    public String generateThumbnailStoragePath() {
        LocalDate now = LocalDate.now();
        String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID().toString() + ".jpg";
        return thumbDir + "/" + dateDir + "/" + filename;
    }
    
    /**
     * 获取文件的完整路径
     */
    public Path getFullPath(String relativePath) {
        return Paths.get(baseDir).resolve(relativePath);
    }
    
    /**
     * 确保目录存在
     */
    public void ensureDirectoryExists(String relativePath) throws IOException {
        Path fullPath = getFullPath(relativePath);
        Path parentDir = fullPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String relativePath) {
        try {
            Path fullPath = getFullPath(relativePath);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("删除文件: {}", relativePath);
            }
        } catch (IOException e) {
            log.warn("删除文件失败: {}, 错误: {}", relativePath, e.getMessage());
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String relativePath) {
        return Files.exists(getFullPath(relativePath));
    }
    
    /**
     * 获取文件大小
     */
    public long getFileSize(String relativePath) throws IOException {
        return Files.size(getFullPath(relativePath));
    }
}
