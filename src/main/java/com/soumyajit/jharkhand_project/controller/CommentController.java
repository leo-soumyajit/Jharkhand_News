package com.soumyajit.jharkhand_project.controller;


import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateCommentRequest;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/district-news/{newsId}")
    public ResponseEntity<ApiResponse<CommentDto>> createDistrictNewsComment(
            @PathVariable Long newsId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto comment = commentService.createDistrictNewsComment(newsId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment created successfully", comment));
        } catch (Exception e) {
            log.error("Error creating district news comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create comment"));
        }
    }

    @PostMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<CommentDto>> createEventComment(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto comment = commentService.createEventComment(eventId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment created successfully", comment));
        } catch (Exception e) {
            log.error("Error creating event comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create comment"));
        }
    }

    @PostMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<CommentDto>> createJobComment(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto comment = commentService.createJobComment(jobId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment created successfully", comment));
        } catch (Exception e) {
            log.error("Error creating job comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create comment"));
        }
    }

    @PostMapping("/community-posts/{postId}")
    public ResponseEntity<ApiResponse<CommentDto>> createCommunityPostComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto comment = commentService.createCommunityPostComment(postId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment created successfully", comment));
        } catch (Exception e) {
            log.error("Error creating community post comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create comment"));
        }
    }

    @GetMapping("/district-news/{newsId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getDistrictNewsComments(@PathVariable Long newsId) {
        try {
            List<CommentDto> comments = commentService.getDistrictNewsComments(newsId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (Exception e) {
            log.error("Error retrieving district news comments", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve comments"));
        }
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getEventComments(@PathVariable Long eventId) {
        try {
            List<CommentDto> comments = commentService.getEventComments(eventId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (Exception e) {
            log.error("Error retrieving event comments", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve comments"));
        }
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getJobComments(@PathVariable Long jobId) {
        try {
            List<CommentDto> comments = commentService.getJobComments(jobId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (Exception e) {
            log.error("Error retrieving job comments", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve comments"));
        }
    }

    @GetMapping("/community-posts/{postId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommunityPostComments(@PathVariable Long postId) {
        try {
            List<CommentDto> comments = commentService.getCommunityPostComments(postId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (Exception e) {
            log.error("Error retrieving community post comments", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve comments"));
        }
    }

    @GetMapping("/{postType}/{postId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsForPost(
            @PathVariable String postType,
            @PathVariable Long postId) {

        try {
            List<CommentDto> comments = commentService.getCommentsForPost(postType, postId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid post type"));
        } catch (Exception e) {
            log.error("Error retrieving comments for {} with ID: {}", postType, postId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve comments"));
        }
    }
}












