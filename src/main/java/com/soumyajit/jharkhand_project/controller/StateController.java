package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.StateDto;
import com.soumyajit.jharkhand_project.dto.CreateStateRequest;
import com.soumyajit.jharkhand_project.service.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/states")  // ✅ FIXED - Changed from /districts
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class StateController {

    private final StateService stateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StateDto>>> getAllStates() {  // ✅ FIXED method name
        try {
            List<StateDto> states = stateService.getAllStates();
            return ResponseEntity.ok(ApiResponse.success("States retrieved successfully", states));
        } catch (Exception e) {
            log.error("Error retrieving states", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve states"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StateDto>> createState(@Valid @RequestBody CreateStateRequest request) {
        try {
            StateDto createdState = stateService.createState(request);
            return ResponseEntity.status(201)
                    .body(ApiResponse.success("State created successfully", createdState));
        } catch (Exception e) {
            log.error("Error creating state", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create state"));
        }
    }
}
