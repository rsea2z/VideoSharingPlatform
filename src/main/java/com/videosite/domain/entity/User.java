package com.videosite.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user", 
       uniqueConstraints = @UniqueConstraint(name = "uk_user_username", columnNames = "username"))
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 100)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String passwordHash;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum Role {
        USER, ADMIN
    }
}
