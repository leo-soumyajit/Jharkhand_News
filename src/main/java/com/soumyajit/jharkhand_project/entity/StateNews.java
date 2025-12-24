package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "state_news")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ✅ NEW: Category field (OPTIONAL for now)
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = true)  // nullable = true means optional
    private NewsCategory category;

    @ElementCollection
    @CollectionTable(name = "state_news_images",
            joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "state_news_cloudinary_ids",
            joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "public_id")
    private List<String> cloudinaryPublicIds = new ArrayList<>();

    @Builder.Default
    private Boolean published = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
