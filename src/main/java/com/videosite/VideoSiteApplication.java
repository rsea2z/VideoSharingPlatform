package com.videosite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.videosite.domain.entity.User;
import com.videosite.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
public class VideoSiteApplication {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(VideoSiteApplication.class, args);
    }

    /**
     * 启动时检查并处理密码加密和FFmpeg
     */
    @Bean
    public CommandLineRunner initializeApplication() {
        return args -> {
            // 检查并加密以Pass_开头的密码
            encryptPlaintextPasswords();
            
            // 检查FFmpeg
            checkFFmpeg();
        };
    }

    private void encryptPlaintextPasswords() {
        log.info("检查需要加密的密码...");
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String passwordHash = user.getPasswordHash();
            if (passwordHash != null && passwordHash.startsWith("Pass_")) {
                // 提取实际密码
                String actualPassword = passwordHash.substring(5); // 去掉"Pass_"前缀
                // 加密密码
                String encryptedPassword = passwordEncoder.encode(actualPassword);
                // 更新用户密码
                user.setPasswordHash(encryptedPassword);
                userRepository.save(user);
                log.info("已加密用户 {} 的密码", user.getUsername());
            }
        }
    }

    private void checkFFmpeg() {
        try {
            // 检查ffmpeg
            Process ffmpegProcess = Runtime.getRuntime().exec(new String[]{"ffmpeg", "-version"});
            int ffmpegResult = ffmpegProcess.waitFor();
            if (ffmpegResult != 0) {
                log.warn("FFmpeg不可用，视频处理功能可能受限");
                return;
            }

            // 检查ffprobe
            Process ffprobeProcess = Runtime.getRuntime().exec(new String[]{"ffprobe", "-version"});
            int ffprobeResult = ffprobeProcess.waitFor();
            if (ffprobeResult != 0) {
                log.warn("FFprobe不可用，视频处理功能可能受限");
                return;
            }

            log.info("FFmpeg和FFprobe检查通过");
        } catch (IOException | InterruptedException e) {
            log.warn("无法检查FFmpeg/FFprobe: {}", e.getMessage());
        }
    }
}
