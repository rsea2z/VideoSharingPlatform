package com.videosite.dto.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadForm {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    @Size(max = 5000, message = "描述长度不能超过5000个字符")
    private String description;
    
    @Size(max = 1000, message = "关键词总长度不能超过1000个字符")
    private String keywords;
    
    @NotNull(message = "请选择要上传的视频文件")
    private MultipartFile videoFile;
    
    /**
     * 检查文件是否为空
     */
    public boolean isVideoFileEmpty() {
        return videoFile == null || videoFile.isEmpty();
    }
    
    /**
     * 获取文件大小（字节）
     */
    public long getVideoFileSize() {
        return videoFile != null ? videoFile.getSize() : 0;
    }
    
    /**
     * 获取原始文件名
     */
    public String getOriginalFilename() {
        return videoFile != null ? videoFile.getOriginalFilename() : null;
    }
}
