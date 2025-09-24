package com.soumyajit.jharkhand_project.controller;


import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.UpdateUserProfileRequest;
import com.soumyajit.jharkhand_project.dto.UserProfileDto;
import com.soumyajit.jharkhand_project.dto.UserStatsDto;
import com.soumyajit.jharkhand_project.dto.UserContentDto;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@AuthenticationPrincipal User user) {
        UserProfileDto profile = userService.getUserProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileDto updatedProfile = userService.updateUserProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDto>> getUserStats(@AuthenticationPrincipal User user) {
        UserStatsDto stats = userService.getUserStats(user);
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", stats));
    }

    @GetMapping("/my-content")
    public ResponseEntity<ApiResponse<UserContentDto>> getMyContent(@AuthenticationPrincipal User user) {
        UserContentDto content = userService.getUserContent(user);
        return ResponseEntity.ok(ApiResponse.success("User content retrieved successfully", content));
    }
}
