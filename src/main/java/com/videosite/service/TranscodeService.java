package com.videosite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodeService {
    
    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    @Value("${app.ffmpeg.ffprobe-path:ffprobe}")
    private String ffprobePath;
    
    @Value("${app.thumbnail.width:320}")
    private int thumbnailWidth;
    
    @Value("${app.thumbnail.height:180}")
    private int thumbnailHeight;
    
    @Value("${app.thumbnail.time-position:5}")
    private int thumbnailTimePosition;
    
    /**
     * 验证视频文件格式
     */
    public VideoInfo validateVideoFile(Path videoPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(ffprobePath);
        command.add("-v");
        command.add("quiet");
        command.add("-print_format");
        command.add("json");
        command.add("-show_format");
        command.add("-show_streams");
        command.add(videoPath.toString());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 设置超时时间为60秒
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("视频文件验证超时（60秒）");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IllegalArgumentException("无法读取视频文件信息");
        }
        
        return parseVideoInfo(output.toString());
    }
    
    /**
     * 生成缩略图
     */
    public void generateThumbnail(Path videoPath, Path thumbnailPath, int durationSeconds) 
            throws IOException, InterruptedException {
        
        // 确定截取时间点（使用更早的时间点，通常视频开头处理更快）
        int timePosition = Math.min(2, durationSeconds / 4); // 改为2秒或1/4处
        if (durationSeconds < 4) {
            timePosition = 1; // 如果视频很短，就取第1秒
        }
        
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(videoPath.toString());
        command.add("-ss");
        command.add(String.valueOf(timePosition));
        command.add("-vframes");
        command.add("1");
        command.add("-vf");
        command.add(String.format("scale=%d:%d:flags=fast_bilinear", thumbnailWidth, thumbnailHeight));
        command.add("-f");
        command.add("mjpeg"); // 指定输出格式
        command.add("-q:v");
        command.add("5"); // 稍微降低质量以提高速度
        command.add("-threads");
        command.add("1"); // 使用单线程，避免资源争用
        command.add("-y"); // 覆盖输出文件
        command.add(thumbnailPath.toString());
        
        log.info("开始生成缩略图，命令: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // 合并错误流和输出流
        Process process = pb.start();
        
        // 读取进程输出（包括错误信息）
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // 设置超时时间为90秒（增加超时时间）
        boolean finished = process.waitFor(90, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            log.error("缩略图生成超时，进程输出: {}", output.toString());
            throw new RuntimeException("生成缩略图超时（90秒）");
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("生成缩略图失败，退出码: {}, FFmpeg输出: {}", exitCode, output.toString());
            throw new RuntimeException("生成缩略图失败");
        }
        
        log.info("成功生成缩略图: {}", thumbnailPath);
    }
    
    /**
     * 解析视频信息
     */
    private VideoInfo parseVideoInfo(String jsonOutput) {
        VideoInfo info = new VideoInfo();
        
        // 解析时长
        Pattern durationPattern = Pattern.compile("\"duration\"\\s*:\\s*\"([^\"]+)\"");
        Matcher durationMatcher = durationPattern.matcher(jsonOutput);
        if (durationMatcher.find()) {
            try {
                double duration = Double.parseDouble(durationMatcher.group(1));
                info.setDurationSeconds((int) Math.round(duration));
            } catch (NumberFormatException e) {
                log.warn("无法解析视频时长: {}", durationMatcher.group(1));
            }
        }
        
        // 检查视频编码
        boolean hasH264Video = jsonOutput.contains("\"codec_name\"") && jsonOutput.contains("h264");
        boolean hasAacAudio = jsonOutput.contains("\"codec_name\"") && jsonOutput.contains("aac");
        
        info.setValidFormat(hasH264Video && hasAacAudio);
        info.setH264Video(hasH264Video);
        info.setAacAudio(hasAacAudio);
        
        return info;
    }
    
    /**
     * 视频信息类
     */
    public static class VideoInfo {
        private int durationSeconds;
        private boolean validFormat;
        private boolean h264Video;
        private boolean aacAudio;
        
        // Getters and Setters
        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        
        public boolean isValidFormat() { return validFormat; }
        public void setValidFormat(boolean validFormat) { this.validFormat = validFormat; }
        
        public boolean isH264Video() { return h264Video; }
        public void setH264Video(boolean h264Video) { this.h264Video = h264Video; }
        
        public boolean isAacAudio() { return aacAudio; }
        public void setAacAudio(boolean aacAudio) { this.aacAudio = aacAudio; }
    }
}
