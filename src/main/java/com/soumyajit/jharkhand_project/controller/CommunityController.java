package com.soumyajit.jharkhand_project.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CommunityPostDto;
import com.soumyajit.jharkhand_project.dto.CreateCommunityPostRequest;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.CommunityPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/community")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityPostService communityPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunityPostDto>>> getApprovedPosts() {
        try {
            List<CommunityPostDto> posts = communityPostService.getApprovedPosts();
            return ResponseEntity.ok(ApiResponse.success("Community posts retrieved successfully", posts));
        } catch (Exception e) {
            log.error("Error retrieving approved community posts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve community posts"));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommunityPostDto>>> getPendingPosts() {
        try {
            List<CommunityPostDto> posts = communityPostService.getPendingPosts();
            return ResponseEntity.ok(ApiResponse.success("Pending posts retrieved successfully", posts));
        } catch (Exception e) {
            log.error("Error retrieving pending community posts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending posts"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommunityPostDto>> getPostById(@PathVariable Long id) {
        try {
            CommunityPostDto post = communityPostService.getPostById(id);
            return ResponseEntity.ok(ApiResponse.success("Community post retrieved successfully", post));
        } catch (Exception e) {
            log.error("Error retrieving community post with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommunityPostDto>> createPost(
            @RequestPart("post") String postJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();

            // Parse JSON string manually
            ObjectMapper objectMapper = new ObjectMapper();
            CreateCommunityPostRequest request = objectMapper.readValue(postJson, CreateCommunityPostRequest.class);

            CommunityPostDto post = communityPostService.createPost(request, images, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Community post created successfully and pending approval", post));
        } catch (Exception e) {
            log.error("Error creating community post", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create community post"));
        }
    }


    @PostMapping("/{postId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommunityPostDto>> approvePost(@PathVariable Long postId) {
        try {
            CommunityPostDto post = communityPostService.approvePost(postId);
            return ResponseEntity.ok(ApiResponse.success("Community post approved successfully", post));
        } catch (Exception e) {
            log.error("Error approving community post with ID: {}", postId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to approve community post"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteCommunityPost(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            communityPostService.deleteCommunityPost(id, user);
            return ResponseEntity.ok(ApiResponse.success("Community post deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting community post with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own posts or be an admin"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete community post"));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<CommunityPostDto>>> getRecentPosts(
            @RequestParam(defaultValue = "15") int days) {
        try {
            List<CommunityPostDto> recentPosts = communityPostService.getRecentPosts(days);
            return ResponseEntity.ok(ApiResponse.success("Recent community posts retrieved successfully", recentPosts));
        } catch (Exception e) {
            log.error("Error retrieving recent community posts for last {} days", days, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent community posts"));
        }
    }


}
