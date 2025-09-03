package com.videosite.service;

import com.videosite.domain.entity.User;
import com.videosite.dto.form.RegisterForm;
import com.videosite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterForm form) {
        // 检查用户名是否已存在
        if (existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (form.getEmail() != null && !form.getEmail().trim().isEmpty() && existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setEmail(form.getEmail());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        log.info("新用户注册成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(User user) {
        return user != null && User.Role.ADMIN.equals(user.getRole());
    }
    
    /**
     * 获取总用户数
     */
    public Long getTotalUserCount() {
        return userRepository.count();
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, String username, String email, String role, Boolean enabled) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 更新用户名
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim());
        }
        
        // 更新邮箱
        if (email != null) {
            user.setEmail(email.trim().isEmpty() ? null : email.trim());
        }
        
        // 更新角色
        if (role != null && !role.trim().isEmpty()) {
            try {
                User.Role newRole = User.Role.valueOf(role.toUpperCase());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的角色: " + role);
            }
        }
        
        // 更新启用状态
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        
        User savedUser = userRepository.save(user);
        log.info("用户信息更新成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 防止删除管理员账户
        if (User.Role.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账户");
        }
        
        userRepository.delete(user);
        log.info("用户删除成功: {}", user.getUsername());
    }
}
