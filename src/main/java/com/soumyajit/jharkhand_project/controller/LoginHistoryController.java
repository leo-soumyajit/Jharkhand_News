package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.entity.LoginHistory;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login-history")
@RequiredArgsConstructor
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping("/my-history")
    public ResponseEntity<?> getMyLoginHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<LoginHistory> history = loginHistoryService.getUserLoginHistory(
                user.getId(), PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("content", history.getContent());
        response.put("totalElements", history.getTotalElements());
        response.put("totalPages", history.getTotalPages());
        response.put("currentPage", history.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentLogins(@AuthenticationPrincipal User user) {
        List<LoginHistory> recentLogins = loginHistoryService.getRecentLoginHistory(user.getId());
        return ResponseEntity.ok(recentLogins);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getTotalLoginCount(@AuthenticationPrincipal User user) {
        Long count = loginHistoryService.getTotalLoginCount(user.getId());
        return ResponseEntity.ok(Map.of("totalLogins", count));
    }
}
