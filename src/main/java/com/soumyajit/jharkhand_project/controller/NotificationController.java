package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.NotificationDto;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getRecentNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int days) {
        User user = (User) authentication.getPrincipal();
        List<NotificationDto> notifications = notificationService.getRecentNotifications(user.getId(), days);
        return ResponseEntity.ok(ApiResponse.success("Recent notifications fetched", notifications));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        notificationService.deleteNotificationByIdAndUser(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully",null));
    }

    // Delete all notifications for the current user
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<Void>> clearAllNotifications(@AuthenticationPrincipal User user) {
        notificationService.deleteAllNotificationsByUser(user);
        return ResponseEntity.ok(ApiResponse.success("All notifications cleared successfully",null));
    }
}
