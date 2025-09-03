package com.videosite.dto.form;

import lombok.Data;

@Data
public class SearchQuery {
    
    private String q; // 搜索关键词
    private int page = 0; // 页码，从0开始
    private int size = 12; // 每页数量
    private String sort = "created_at"; // 排序字段
    private String order = "desc"; // 排序方向
    
    /**
     * 获取清理后的搜索关键词
     */
    public String getCleanQuery() {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        return q.trim();
    }
    
    /**
     * 检查是否有搜索条件
     */
    public boolean hasQuery() {
        return getCleanQuery() != null;
    }
}
