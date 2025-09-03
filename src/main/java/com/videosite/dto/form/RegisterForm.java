package com.videosite.dto.form;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterForm {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在8-64字符之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 验证两次密码是否一致
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(confirmPassword);
    }
}
