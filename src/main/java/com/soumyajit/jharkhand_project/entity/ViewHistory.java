package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "view_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_type", "content_id"}),
        indexes = {
                @Index(name = "idx_user_content_type", columnList = "user_id, content_type"),
                @Index(name = "idx_viewed_at", columnList = "viewed_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public enum ContentType {
        STATE_NEWS,
        EVENT,
        JOB,
        COMMUNITY,
        PROPERTY
    }
}
