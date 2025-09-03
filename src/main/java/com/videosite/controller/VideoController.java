package com.videosite.controller;

import com.videosite.domain.entity.User;
import com.videosite.domain.entity.Video;
import com.videosite.dto.form.UploadForm;
import com.videosite.security.CustomUserDetailsService.CustomUserPrincipal;
import com.videosite.service.StorageService;
import com.videosite.service.UserService;
import com.videosite.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VideoController {
    
    private final VideoService videoService;
    private final UserService userService;
    private final StorageService storageService;
    
    /**
     * 视频详情页
     */
    @GetMapping("/videos/{id}")
    public String videoDetail(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal CustomUserPrincipal principal,
                             RedirectAttributes redirectAttributes) {
        
        // 检查用户是否已登录
        if (principal == null) {
            // 未登录用户，重定向到登录页面并保存原始URL
            redirectAttributes.addFlashAttribute("message", "请登录后查看视频详情");
            return "redirect:/login?redirectUrl=/videos/" + id;
        }
        
        Optional<Video> videoOpt = videoService.findById(id);
        if (videoOpt.isEmpty()) {
            throw new RuntimeException("视频不存在");
        }
        
        Video video = videoOpt.get();
        model.addAttribute("video", video);
        
        // 获取上传者信息
        Optional<User> uploaderOpt = userService.findById(video.getUploaderId());
        uploaderOpt.ifPresent(uploader -> model.addAttribute("uploader", uploader));
        
        // 用户已登录
        model.addAttribute("isLoggedIn", true);
        
        // 检查当前用户是否可以删除此视频
        User currentUser = principal.getUser();
        boolean canDelete = video.getUploaderId().equals(currentUser.getId()) || 
                           userService.isAdmin(currentUser);
        model.addAttribute("canDelete", canDelete);
        
        return "videos/detail";
    }
    
    /**
     * 视频流接口（支持Range请求）
     */
    @GetMapping("/stream/{id}")
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable Long id,
                                                          @RequestHeader(value = "Range", required = false) String rangeHeader,
                                                          @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Video> videoOpt = videoService.findById(id);
        if (videoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Video video = videoOpt.get();
        Path videoPath = storageService.getFullPath(video.getStoragePath());
        
        if (!Files.exists(videoPath)) {
            log.error("视频文件不存在: {}", videoPath);
            return ResponseEntity.notFound().build();
        }
        
        try {
            long fileSize = Files.size(videoPath);
            
            // 记录播放（首次播放或包含字节0的Range请求）
            if (rangeHeader == null || rangeHeader.contains("bytes=0-")) {
                videoService.recordView(id);
            }
            
            // 简化的Range处理（实际生产环境需要更完善的Range处理）
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 这里简化处理，实际应该解析Range头并返回部分内容
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE, "bytes 0-" + (fileSize - 1) + "/" + fileSize)
                        .body(new InputStreamResource(new FileInputStream(videoPath.toFile())));
            } else {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .body(new InputStreamResource(new FileInputStream(videoPath.toFile())));
            }
            
        } catch (IOException e) {
            log.error("读取视频文件失败: {}", videoPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 视频下载接口
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadVideo(@PathVariable Long id,
                                                           @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Video> videoOpt = videoService.findById(id);
        if (videoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Video video = videoOpt.get();
        Path videoPath = storageService.getFullPath(video.getStoragePath());
        
        if (!Files.exists(videoPath)) {
            log.error("视频文件不存在: {}", videoPath);
            return ResponseEntity.notFound().build();
        }
        
        try {
            // 记录下载
            videoService.recordDownload(id);
            
            long fileSize = Files.size(videoPath);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + video.getOriginalFilename() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                    .body(new InputStreamResource(new FileInputStream(videoPath.toFile())));
            
        } catch (IOException e) {
            log.error("下载视频文件失败: {}", videoPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 缩略图接口
     */
    @GetMapping("/thumbnails/{id}")
    public ResponseEntity<InputStreamResource> getThumbnail(@PathVariable Long id) {
        Optional<Video> videoOpt = videoService.findById(id);
        if (videoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Video video = videoOpt.get();
        Path thumbPath = storageService.getFullPath(video.getThumbPath());
        
        if (!Files.exists(thumbPath)) {
            log.warn("缩略图文件不存在: {}", thumbPath);
            return ResponseEntity.notFound().build();
        }
        
        try {
            long fileSize = Files.size(thumbPath);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                    .body(new InputStreamResource(new FileInputStream(thumbPath.toFile())));
            
        } catch (IOException e) {
            log.error("读取缩略图失败: {}", thumbPath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 上传页面
     */
    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("uploadForm", new UploadForm());
        return "videos/upload";
    }
    
    /**
     * 处理视频上传
     */
    @PostMapping("/upload")
    public String uploadVideo(@Valid @ModelAttribute UploadForm uploadForm,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserPrincipal principal,
                             RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "videos/upload";
        }
        
        if (uploadForm.isVideoFileEmpty()) {
            bindingResult.rejectValue("videoFile", "error.videoFile", "请选择要上传的视频文件");
            return "videos/upload";
        }
        
        try {
            User currentUser = principal.getUser();
            Video video = videoService.uploadVideo(uploadForm, currentUser.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "视频上传成功！");
            return "redirect:/videos/" + video.getId();
            
        } catch (Exception e) {
            log.error("视频上传失败", e);
            bindingResult.reject("error.upload", "视频上传失败: " + e.getMessage());
            return "videos/upload";
        }
    }
    
    /**
     * 删除视频
     */
    @PostMapping("/videos/{id}/delete")
    public String deleteVideo(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserPrincipal principal,
                             RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = principal.getUser();
            boolean isAdmin = userService.isAdmin(currentUser);
            
            videoService.deleteVideo(id, currentUser.getId(), isAdmin);
            
            redirectAttributes.addFlashAttribute("successMessage", "视频删除成功！");
            return "redirect:/";
            
        } catch (Exception e) {
            log.error("删除视频失败: id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败: " + e.getMessage());
            return "redirect:/videos/" + id;
        }
    }
}
