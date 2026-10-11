package com.datn.fashion.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_style_preferences")
public class UserStylePreferences {
    public enum Status { NOT_STARTED, SKIPPED, COMPLETED }

    @Id @Column(name = "user_id") public Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    public Status status = Status.NOT_STARTED;
    @JdbcTypeCode(SqlTypes.TIMESTAMP) @Column(name = "completed_at", columnDefinition = "TIMESTAMP")
    public Instant completedAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP) @Column(name = "skipped_at", columnDefinition = "TIMESTAMP")
    public Instant skippedAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP) @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    public Instant createdAt;
    @JdbcTypeCode(SqlTypes.TIMESTAMP) @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    public Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "user_preferred_styles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "style_id", nullable = false)
    public Set<Long> styleIds = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "user_preferred_colors", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "color_id", nullable = false)
    public Set<Long> colorIds = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "user_preferred_occasions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "occasion_id", nullable = false)
    public Set<Long> occasionIds = new LinkedHashSet<>();

    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
