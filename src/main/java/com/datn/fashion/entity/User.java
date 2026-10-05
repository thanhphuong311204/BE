package com.datn.fashion.entity;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
    @UniqueConstraint(name = "uq_users_phone_number", columnNames = "phone_number")
})
public class User {
    public enum Status { ACTIVE, INACTIVE, SUSPENDED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, length = 254)
    public String email;
    @Column(name = "phone_number", length = 20)
    public String phoneNumber;
    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    public Status status = Status.ACTIVE;
    @Column(name = "email_verified", nullable = false)
    public boolean emailVerified;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "email_verified_at", columnDefinition = "TIMESTAMP")
    public Instant emailVerifiedAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "last_login_at", columnDefinition = "TIMESTAMP")
    public Instant lastLoginAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    public Instant createdAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    public Instant updatedAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    public Instant deletedAt;

    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
