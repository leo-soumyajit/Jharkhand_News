package com.soumyajit.jharkhand_project.controller;

import com.cloudinary.Api;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Object>> healthCheck(){
        ApiResponse apiResponse = new ApiResponse<>();
        return ResponseEntity.ok(apiResponse.builder()
                .message("OK healthy").build());
    }
}
