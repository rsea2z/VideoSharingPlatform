-- V2__add_video_notes.sql
-- 创建视频笔记表，支持基于时间锚点的交互式学习功能

CREATE TABLE IF NOT EXISTS video_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    timestamp_seconds INT NOT NULL COMMENT '视频时间戳（秒）',
    content TEXT NOT NULL COMMENT '笔记内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (video_id) REFERENCES video(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频笔记表';
