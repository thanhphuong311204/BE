package com.datn.fashion.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    public enum Gender { FEMALE, MALE, OTHER, PREFER_NOT_TO_SAY }

    @Id @Column(name = "user_id")
    public Long userId;
    @MapsId @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;
    @Column(name = "full_name", nullable = false, length = 150)
    public String fullName;
    @Column(name = "avatar_url", length = 1000)
    public String avatarUrl;
    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    public Gender gender;
    @Column(length = 500)
    public String bio;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    public Instant createdAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    public Instant updatedAt;

    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
