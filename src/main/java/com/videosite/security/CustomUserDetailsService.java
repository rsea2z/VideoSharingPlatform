package com.videosite.security;

import com.videosite.domain.entity.User;
import com.videosite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        return new CustomUserPrincipal(user);
    }
    
    /**
     * 自定义用户主体类
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private final User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String role = "ROLE_" + user.getRole().name();
            return List.of(new SimpleGrantedAuthority(role));
        }
        
        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }
        
        @Override
        public String getUsername() {
            return user.getUsername();
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return user.getEnabled();
        }
        
        // 获取用户实体
        public User getUser() {
            return user;
        }
        
        // 获取用户ID
        public Long getUserId() {
            return user.getId();
        }
        
        // 检查是否为管理员
        public boolean isAdmin() {
            return User.Role.ADMIN.equals(user.getRole());
        }
    }
}
