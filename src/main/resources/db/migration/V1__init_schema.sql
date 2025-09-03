-- 初始化数据库表结构

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名，3-20位，字母数字下划线',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    `email` VARCHAR(255) NULL COMMENT '邮箱，可选',
    `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 视频表
CREATE TABLE `video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL COMMENT '视频标题',
    `description` TEXT NULL COMMENT '视频描述',
    `keywords` VARCHAR(1000) NULL COMMENT '关键词，逗号分隔',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `thumb_path` VARCHAR(500) NOT NULL COMMENT '缩略图路径',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
    `size_bytes` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `duration_seconds` INT NOT NULL COMMENT '视频时长（秒）',
    `views_total` BIGINT NOT NULL DEFAULT 0 COMMENT '总播放次数',
    `downloads_total` BIGINT NOT NULL DEFAULT 0 COMMENT '总下载次数',
    `visibility` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '可见性',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_video_title` (`title`),
    KEY `idx_video_uploader_id` (`uploader_id`),
    KEY `idx_video_created_at` (`created_at`),
    KEY `idx_video_views_total` (`views_total`),
    KEY `idx_video_downloads_total` (`downloads_total`),
    CONSTRAINT `fk_video_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频表';

-- 视频日统计表
CREATE TABLE `video_daily_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `views` INT NOT NULL DEFAULT 0 COMMENT '当日播放次数',
    `downloads` INT NOT NULL DEFAULT 0 COMMENT '当日下载次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_video_daily_stats` (`video_id`, `stat_date`),
    KEY `idx_video_daily_stats_date` (`stat_date`),
    CONSTRAINT `fk_video_daily_stats_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频日统计表';

-- 插入默认管理员账号  
-- 密码使用Pass_前缀标记需要加密，实际密码为: 1234qwer
INSERT INTO `user` (`username`, `password_hash`, `email`, `role`, `enabled`) VALUES 
('admin', 'Pass_1234qwer', 'admin@example.com', 'ADMIN', 1);
