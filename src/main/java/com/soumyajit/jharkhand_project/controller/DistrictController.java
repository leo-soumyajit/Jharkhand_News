package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.DistrictDto;
import com.soumyajit.jharkhand_project.dto.CreateDistrictRequest;
import com.soumyajit.jharkhand_project.service.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/districts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DistrictDto>>> getAllDistricts() {
        try {
            List<DistrictDto> districts = districtService.getAllDistricts();
            return ResponseEntity.ok(ApiResponse.success("Districts retrieved successfully", districts));
        } catch (Exception e) {
            log.error("Error retrieving districts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve districts"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DistrictDto>> createDistrict(@Valid @RequestBody CreateDistrictRequest request) {
        try {
            DistrictDto createdDistrict = districtService.createDistrict(request);
            return ResponseEntity.status(201)
                    .body(ApiResponse.success("District created successfully", createdDistrict));
        } catch (Exception e) {
            log.error("Error creating district", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create district"));
        }
    }
}
