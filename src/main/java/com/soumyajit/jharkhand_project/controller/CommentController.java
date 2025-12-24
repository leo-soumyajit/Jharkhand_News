package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreateCommentRequest;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.exception.EntityNotFoundException;
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

    @PostMapping("/state-news/{newsId}")  // ✅ FIXED endpoint
    public ResponseEntity<ApiResponse<CommentDto>> createStateNewsComment(  // ✅ FIXED method name
                                                                            @PathVariable Long newsId,
                                                                            @Valid @RequestBody CreateCommentRequest request,
                                                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto comment = commentService.createStateNewsComment(newsId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment created successfully", comment));
        } catch (Exception e) {
            log.error("Error creating state news comment", e);  // ✅ FIXED log message
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

    @GetMapping("/state-news/{newsId}")  // ✅ FIXED endpoint
    public ResponseEntity<ApiResponse<List<CommentDto>>> getStateNewsComments(@PathVariable Long newsId) {  // ✅ FIXED method name
        try {
            List<CommentDto> comments = commentService.getStateNewsComments(newsId);
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
        } catch (Exception e) {
            log.error("Error retrieving state news comments", e);  // ✅ FIXED log message
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

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CommentDto updatedComment = commentService.updateComment(commentId, request, user);
            return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", updatedComment));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update comment"));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            commentService.deleteComment(commentId, user);
            return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting comment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete comment"));
        }
    }
}
