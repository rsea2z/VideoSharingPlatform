package com.videosite.controller;

import com.videosite.dto.form.LoginForm;
import com.videosite.dto.form.RegisterForm;
import com.videosite.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginForm(@ModelAttribute LoginForm loginForm, 
                           @RequestParam(required = false) String redirectUrl,
                           Model model) {
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            model.addAttribute("redirectUrl", redirectUrl);
        }
        return "auth/login";
    }
    
    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerForm(@ModelAttribute RegisterForm registerForm, Model model) {
        return "auth/register";
    }
    
    /**
     * 处理用户注册
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        
        // 检查表单验证错误
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // 检查两次密码是否一致
        if (!registerForm.isPasswordMatched()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "两次输入的密码不一致");
            return "auth/register";
        }
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerForm.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "用户名已存在");
            return "auth/register";
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (registerForm.getEmail() != null && !registerForm.getEmail().trim().isEmpty() 
            && userService.existsByEmail(registerForm.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "邮箱已被使用");
            return "auth/register";
        }
        
        try {
            userService.register(registerForm);
            log.info("用户注册成功: {}", registerForm.getUsername());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "注册成功！请使用您的用户名和密码登录。");
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("用户注册失败: {}", registerForm.getUsername(), e);
            bindingResult.reject("error.register", "注册失败: " + e.getMessage());
            return "auth/register";
        }
    }
}
