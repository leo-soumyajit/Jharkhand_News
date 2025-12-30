package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;        // 🆕 Changed
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ REQUIRED - Title
    @Column(nullable = false)
    @NotBlank(message = "Event title is required")
    private String title;

    // ✅ REQUIRED - Description
    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Event description is required")
    private String description;

    // ⚠️ OPTIONAL - Registration Link
    private String reglink;

    // ⚠️ OPTIONAL - Event Start Date & Time
    private LocalDateTime eventDate;

    // 🆕 OPTIONAL - Event End Date (DATE ONLY, no time)
    private LocalDate endDate;

    // ⚠️ OPTIONAL - Location
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ✅ REQUIRED - At least one image
    @ElementCollection
    @CollectionTable(name = "event_images",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    @NotEmpty(message = "At least one event image is required")
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
