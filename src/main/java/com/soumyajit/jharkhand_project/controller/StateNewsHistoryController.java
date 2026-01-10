package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.ViewHistoryDto;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.entity.ViewHistory;
import com.soumyajit.jharkhand_project.service.ViewHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/history/state-news")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class StateNewsHistoryController {

    private final ViewHistoryService viewHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ViewHistoryDto>>> getStateNewsHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<ViewHistoryDto> history = viewHistoryService.getHistoryWithDetails(
                    user, ViewHistory.ContentType.STATE_NEWS, PageRequest.of(page, size));

            return ResponseEntity.ok(
                    ApiResponse.success("State news history retrieved successfully", history));
        } catch (Exception e) {
            log.error("Error retrieving state news history for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve history"));
        }
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<ApiResponse<String>> deleteStateNewsHistory(
            @AuthenticationPrincipal User user,
            @PathVariable Long newsId) {

        try {
            viewHistoryService.deleteSingleHistory(
                    user, ViewHistory.ContentType.STATE_NEWS, newsId);

            return ResponseEntity.ok(
                    ApiResponse.success("History item deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting state news history for user: {} and newsId: {}",
                    user.getId(), newsId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete history item"));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearAllStateNewsHistory(
            @AuthenticationPrincipal User user) {

        try {
            viewHistoryService.clearAllHistory(user, ViewHistory.ContentType.STATE_NEWS);

            return ResponseEntity.ok(
                    ApiResponse.success("All state news history cleared successfully", null));
        } catch (Exception e) {
            log.error("Error clearing all state news history for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to clear history"));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStateNewsHistoryCount(
            @AuthenticationPrincipal User user) {

        try {
            Long count = viewHistoryService.getHistoryCount(
                    user, ViewHistory.ContentType.STATE_NEWS);

            return ResponseEntity.ok(
                    ApiResponse.success("History count retrieved successfully", Map.of("count", count)));
        } catch (Exception e) {
            log.error("Error getting state news history count for user: {}", user.getId(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get history count"));
        }
    }
}
