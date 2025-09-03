package com.videosite.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeywordUtils {
    
    /**
     * 解析关键词字符串，返回标准化的关键词列表
     */
    public static List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return List.of();
        }
        
        // 使用正则表达式分割，支持多种分隔符
        String[] tokens = keywords.split("[,，;\\s#]+");
        
        Set<String> keywordSet = new LinkedHashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                keywordSet.add(trimmed);
            }
        }
        
        return keywordSet.stream().collect(Collectors.toList());
    }
    
    /**
     * 标准化关键词字符串，用于存储
     */
    public static String normalizeKeywords(String keywords) {
        List<String> parsed = parseKeywords(keywords);
        return String.join(",", parsed);
    }
    
    /**
     * 检查关键词是否匹配
     */
    public static boolean matchesKeywords(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }
    
    /**
     * 计算匹配的关键词数量
     */
    public static long countMatches(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return 0;
        }
        
        String lowerText = text.toLowerCase();
        return keywords.stream()
                .mapToLong(keyword -> lowerText.contains(keyword.toLowerCase()) ? 1 : 0)
                .sum();
    }
    
    /**
     * 检查是否所有关键词都匹配（AND逻辑）
     */
    public static boolean matchesAllKeywords(String text, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        
        if (text == null) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        return keywords.stream()
                .allMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }
}
