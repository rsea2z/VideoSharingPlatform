package com.videosite.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "1234qwer";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // 验证哈希
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
        
        // 检查现有的哈希
        String existingHash = "$2a$10$9OuIkqGRvNL9uYhQj5Z1YO4LzKX.k6rNrZ5rjJ9ZP3bQ3Uu5oY.XS";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("Existing hash matches: " + matchesExisting);
    }
}
