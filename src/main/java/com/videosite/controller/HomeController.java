package com.videosite.controller;

import com.videosite.domain.entity.Video;
import com.videosite.dto.form.SearchQuery;
import com.videosite.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final VideoService videoService;
    
    /**
     * 首页 - 显示视频列表
     */
    @GetMapping("/")
    public String home(@ModelAttribute SearchQuery searchQuery, Model model) {
        // 设置默认分页参数
        if (searchQuery.getSize() <= 0) {
            searchQuery.setSize(12);
        }
        
        Pageable pageable = PageRequest.of(searchQuery.getPage(), searchQuery.getSize());
        Page<Video> videos;
        
        if (searchQuery.hasQuery()) {
            videos = videoService.searchVideos(searchQuery);
            model.addAttribute("query", searchQuery.getQ());
        } else {
            videos = videoService.findAllVideos(pageable);
        }
        
        model.addAttribute("videos", videos);
        model.addAttribute("searchQuery", searchQuery);
        
        return "index";
    }
    
    /**
     * 视频列表页
     */
    @GetMapping("/videos")
    public String videos(@ModelAttribute SearchQuery searchQuery, Model model) {
        // 设置默认分页参数
        if (searchQuery.getSize() <= 0) {
            searchQuery.setSize(12);
        }
        
        Page<Video> videos;
        
        if (searchQuery.hasQuery()) {
            videos = videoService.searchVideos(searchQuery);
            model.addAttribute("query", searchQuery.getQ());
        } else {
            Pageable pageable = PageRequest.of(searchQuery.getPage(), searchQuery.getSize());
            videos = videoService.findAllVideos(pageable);
        }
        
        model.addAttribute("videos", videos);
        model.addAttribute("searchQuery", searchQuery);
        
        return "videos/list";
    }
}
