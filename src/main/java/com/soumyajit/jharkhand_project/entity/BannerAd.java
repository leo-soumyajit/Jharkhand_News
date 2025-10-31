package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "banner_ads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Banner file URL or storage path
    private String bannerUrl;

    private String destinationUrl;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(name = "impressions_count", nullable = false)
    private long impressionsCount = 0;


    @Enumerated(EnumType.STRING)
    private Size size;


    @Enumerated(EnumType.STRING)
    private Status status;

    private String publicId;


    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum Size {
        SMALL,
        LARGE
    }
}
