package com.soumyajit.jharkhand_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreateJobRequest;
import com.soumyajit.jharkhand_project.dto.JobDto;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.JobService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/jobs")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobDto>>> getApprovedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("GET /api/v1/jobs - page: {}, size: {}", page, size);

            // Validate page and size
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number cannot be negative"));
            }

            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 100"));
            }

            // Create Pageable object (sorting already in repository method)
            Pageable pageable = PageRequest.of(page, size);

            // Fetch paginated jobs
            Page<JobDto> jobs = jobService.getApprovedJobs(pageable);

            log.info("Successfully retrieved {} jobs", jobs.getNumberOfElements());

            return ResponseEntity.ok(
                    ApiResponse.success("Jobs retrieved successfully", jobs)
            );

        } catch (Exception e) {
            log.error("Error retrieving approved jobs - page: {}, size: {}", page, size, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve jobs"));
        }
    }


    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<JobDto>>> getPendingJobs() {
        try {
            List<JobDto> jobs = jobService.getPendingJobs();
            return ResponseEntity.ok(ApiResponse.success("Pending jobs retrieved successfully", jobs));
        } catch (Exception e) {
            log.error("Error retrieving pending jobs", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending jobs"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(@PathVariable Long id) {
        try {
            JobDto job = jobService.getJobById(id);
            return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", job));
        } catch (Exception e) {
            log.error("Error retrieving job with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobDto>> createJob(
            @RequestPart("job") String jobJson,
            @RequestPart(value = "images", required = true) List<MultipartFile> images,  // Make images required
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();

            // Parse JSON string manually
            CreateJobRequest request = objectMapper.readValue(jobJson, CreateJobRequest.class);

            // Validate that at least one image is provided
            if (images == null || images.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("At least one image is required"));
            }

            JobDto job = jobService.createJob(request, images, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Job created successfully and pending approval", job));
        } catch (Exception e) {
            log.error("Error creating job", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create job: " + e.getMessage()));
        }
    }


    @PostMapping("/{jobId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobDto>> approveJob(@PathVariable Long jobId) {
        try {
            JobDto job = jobService.approveJob(jobId);
            return ResponseEntity.ok(ApiResponse.success("Job approved successfully", job));
        } catch (Exception e) {
            log.error("Error approving job with ID: {}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to approve job"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteJob(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            jobService.deleteJob(id, user);
            return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting job with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own jobs or be an admin"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete job"));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<JobDto>>> getRecentJobs(
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<JobDto> recentJobs = jobService.getRecentJobs(days);
            return ResponseEntity.ok(ApiResponse.success("Recent jobs retrieved successfully", recentJobs));
        } catch (Exception e) {
            log.error("Error retrieving recent jobs for last {} days", days, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent jobs"));
        }
    }


}
