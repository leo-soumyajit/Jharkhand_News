package com.soumyajit.jharkhand_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.BannerAdDto;
import com.soumyajit.jharkhand_project.entity.BannerAd.Status;
import com.soumyajit.jharkhand_project.service.BannerAdService;
import com.soumyajit.jharkhand_project.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/banner-ads")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BannerAdController {

    private final BannerAdService bannerAdService;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BannerAdDto>>> getAllAds() {
        try {
            List<BannerAdDto> ads = bannerAdService.getAllAds();
            return ResponseEntity.ok(ApiResponse.success("Banner ads retrieved", ads));
        } catch (Exception e) {
            log.error("Error fetching banner ads", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve banner ads"));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BannerAdDto>>> getActiveAds() {
        try {
            List<BannerAdDto> activeAds = bannerAdService.getActiveAds();
            return ResponseEntity.ok(ApiResponse.success("Active banner ads", activeAds));
        } catch (Exception e) {
            log.error("Error fetching active banner ads", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active banner ads"));
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerAdDto>> createBannerAd(
            @RequestPart("bannerImage") MultipartFile bannerImage,
            @RequestPart("metadata") String bannerMetadataJson) {
        try {

            var uploadResult = cloudinaryService.uploadImageWithPublicId(bannerImage);


            BannerAdDto dto = objectMapper.readValue(bannerMetadataJson, BannerAdDto.class);

            dto.setBannerUrl(uploadResult.getUrl());
            dto.setPublicId(uploadResult.getPublicId());

            BannerAdDto created = bannerAdService.createBannerAd(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Banner ad created with image upload", created));
        } catch (Exception e) {
            log.error("Error creating banner with image upload", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create banner ad"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerAdDto>> updateBannerAd(
            @PathVariable Long id,
            @RequestBody BannerAdDto dto) {
        try {

            BannerAdDto existing = bannerAdService.getBannerAdById(id);
            dto.setBannerUrl(existing.getBannerUrl());
            dto.setPublicId(existing.getPublicId());

            BannerAdDto updated = bannerAdService.updateBannerAd(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Banner ad updated", updated));
        } catch (Exception e) {
            log.error("Error updating banner ad", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update banner ad"));
        }
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerAdDto>> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Status newStatus = Status.valueOf(status.toUpperCase());
            BannerAdDto updated = bannerAdService.changeStatus(id, newStatus);
            return ResponseEntity.ok(ApiResponse.success("Banner ad status updated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status value"));
        } catch (Exception e) {
            log.error("Error changing banner status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to change banner status"));
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteBannerAd(@PathVariable Long id) {
        try {
            bannerAdService.deleteBannerAd(id);
            return ResponseEntity.ok(ApiResponse.success("Banner ad deleted", null));
        } catch (Exception e) {
            log.error("Error deleting banner ad with id {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete banner ad"));
        }
    }
}
