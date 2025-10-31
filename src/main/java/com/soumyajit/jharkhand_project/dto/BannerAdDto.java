package com.soumyajit.jharkhand_project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerAdDto {
    private Long id;
    private String title;
    private String bannerUrl;
    private String destinationUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String size;
    private String publicId;
}
