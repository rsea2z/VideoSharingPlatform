package com.videosite.controller;

import com.videosite.domain.entity.User;
import com.videosite.domain.entity.Video;
import com.videosite.service.StatsService;
import com.videosite.service.UserService;
import com.videosite.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final VideoService videoService;
    private final StatsService statsService;
    private final UserService userService;
    
    /**
     * 管理后台首页
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 获取Top10视频
        List<Video> topViewedVideos = videoService.getTopViewedVideos();
        List<Video> topDownloadedVideos = videoService.getTopDownloadedVideos();
        
        // 获取统计摘要
        Long totalViews = statsService.getTotalViews() != null ? statsService.getTotalViews() : 0L;
        Long totalDownloads = statsService.getTotalDownloads() != null ? statsService.getTotalDownloads() : 0L;
        Long totalUsers = userService.getTotalUserCount();
        Long totalVideos = videoService.getTotalVideoCount();
        
        // 获取所有用户和视频（用于管理）
        List<User> allUsers = userService.getAllUsers();
        List<Video> allVideos = videoService.getAllVideos();
        
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalDownloads", totalDownloads);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalVideos", totalVideos);
        model.addAttribute("topViewedVideos", topViewedVideos);
        model.addAttribute("topDownloadedVideos", topDownloadedVideos);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allVideos", allVideos);
        
        return "admin/dashboard";
    }
    
    /**
     * 获取每日播放统计数据（API）
     */
    @GetMapping("/api/stats/daily-views")
    public ResponseEntity<Map<String, Object>> getDailyViewsStats() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 最近7天
        
        List<Object[]> stats = statsService.getDailyViewsStats(startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> dailyStats = new HashMap<>();
        
        // 初始化7天的数据
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyStats.put(date.toString(), 0L);
        }
        
        // 填入实际数据
        for (Object[] stat : stats) {
            LocalDate date = (LocalDate) stat[0];
            Long views = ((Number) stat[1]).longValue();
            dailyStats.put(date.toString(), views);
        }
        
        result.put("dailyStats", dailyStats);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取每日下载统计数据（API）
     */
    @GetMapping("/api/stats/daily-downloads")
    public ResponseEntity<Map<String, Object>> getDailyDownloadsStats() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 最近7天
        
        List<Object[]> stats = statsService.getDailyDownloadsStats(startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> dailyStats = new HashMap<>();
        
        // 初始化7天的数据
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyStats.put(date.toString(), 0L);
        }
        
        // 填入实际数据
        for (Object[] stat : stats) {
            LocalDate date = (LocalDate) stat[0];
            Long downloads = ((Number) stat[1]).longValue();
            dailyStats.put(date.toString(), downloads);
        }
        
        result.put("dailyStats", dailyStats);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取用户详细信息（API）
     */
    @GetMapping("/api/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新用户信息（API）
     */
    @PostMapping("/api/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, 
                                                          @RequestParam String username,
                                                          @RequestParam(required = false) String email,
                                                          @RequestParam(required = false) String role,
                                                          @RequestParam(required = false) Boolean enabled) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findById(id);
            if (!userOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 检查用户名是否被其他用户使用
            if (!user.getUsername().equals(username) && userService.existsByUsername(username)) {
                result.put("success", false);
                result.put("message", "用户名已被其他用户使用");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查邮箱是否被其他用户使用
            if (email != null && !email.trim().isEmpty() && 
                !email.equals(user.getEmail()) && userService.existsByEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱已被其他用户使用");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 更新用户信息
            User updatedUser = userService.updateUser(id, username, email, role, enabled);
            result.put("success", true);
            result.put("message", "用户信息更新成功");
            result.put("user", updatedUser);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 删除用户（API）
     */
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findById(id);
            if (!userOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 防止删除管理员账户
            if (User.Role.ADMIN.equals(user.getRole())) {
                result.put("success", false);
                result.put("message", "不能删除管理员账户");
                return ResponseEntity.badRequest().body(result);
            }
            
            userService.deleteUser(id);
            result.put("success", true);
            result.put("message", "用户删除成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
