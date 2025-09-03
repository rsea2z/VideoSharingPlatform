package com.videosite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 公开访问的路径
                .requestMatchers("/", "/home", "/search", "/videos", "/videos/**", "/thumbnails/**").permitAll()
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/vendor/**").permitAll()
                
                // 需要登录的路径（视频流和下载需要登录）
                .requestMatchers("/stream/**", "/download/**").authenticated()
                .requestMatchers("/upload", "/profile/**").authenticated()
                
                // 管理员路径
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    String redirectUrl = request.getParameter("redirectUrl");
                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        response.sendRedirect(redirectUrl);
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("video-site-remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7天
            )
            .sessionManagement(session -> session
                .maximumSessions(5) // 允许同时5个会话
                .maxSessionsPreventsLogin(false)
            );
            
        // 禁用CSRF（简化开发，生产环境建议启用）
        http.csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
