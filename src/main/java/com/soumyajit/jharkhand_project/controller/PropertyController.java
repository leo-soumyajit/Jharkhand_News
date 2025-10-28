package com.soumyajit.jharkhand_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreatePropertyRequest;
import com.soumyajit.jharkhand_project.dto.PropertyDto;
import com.soumyajit.jharkhand_project.dto.PropertySearchRequest;
import com.soumyajit.jharkhand_project.entity.Property;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/properties")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyDto>>> getApprovedProperties() {
        try {
            List<PropertyDto> properties = propertyService.getApprovedProperties();
            return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
        } catch (Exception e) {
            log.error("Error retrieving approved properties", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve properties"));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> getPendingProperties() {
        try {
            List<PropertyDto> properties = propertyService.getPendingProperties();
            return ResponseEntity.ok(ApiResponse.success("Pending properties retrieved successfully", properties));
        } catch (Exception e) {
            log.error("Error retrieving pending properties", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve pending properties"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyDto>> getPropertyById(@PathVariable Long id) {
        try {
            PropertyDto property = propertyService.getPropertyById(id);
            return ResponseEntity.ok(ApiResponse.success("Property retrieved successfully", property));
        } catch (Exception e) {
            log.error("Error retrieving property with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyDto>> createProperty(
            @RequestPart("property") String propertyJson,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "floorPlans", required = false) List<MultipartFile> floorPlans,
            Authentication authentication) {

        try {
            User user = (User) authentication.getPrincipal();
            CreatePropertyRequest request = objectMapper.readValue(propertyJson, CreatePropertyRequest.class);

            PropertyDto property = propertyService.createProperty(request, images, floorPlans, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Property created successfully and pending approval", property));
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating property", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating property", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create property: " + e.getMessage()));
        }
    }

    @PostMapping("/{propertyId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyDto>> approveProperty(@PathVariable Long propertyId) {
        try {
            PropertyDto property = propertyService.approveProperty(propertyId);
            return ResponseEntity.ok(ApiResponse.success("Property approved successfully", property));
        } catch (Exception e) {
            log.error("Error approving property with ID: {}", propertyId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to approve property"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteProperty(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            propertyService.deleteProperty(id, user);
            return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting property with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own properties"));
            }
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete property"));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyDto>> updatePropertyStatus(
            @PathVariable Long id,
            @RequestParam Property.PropertyStatus status,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            PropertyDto property = propertyService.updatePropertyStatus(id, status, user);
            return ResponseEntity.ok(ApiResponse.success("Property status updated successfully", property));
        } catch (Exception e) {
            log.error("Error updating property status with ID: {}", id, e);
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only update your own properties"));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> getRecentProperties(
            @RequestParam(defaultValue = "15") int days) {
        try {
            List<PropertyDto> recentProperties = propertyService.getRecentProperties(days);
            return ResponseEntity.ok(ApiResponse.success("Recent properties retrieved successfully", recentProperties));
        } catch (Exception e) {
            log.error("Error retrieving recent properties for last {} days", days, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve recent properties"));
        }
    }

    @GetMapping("/my-properties")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> getMyProperties(Authentication authentication) {
        try {
            if (authentication == null) {
                log.error("Authentication is NULL!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Not authenticated"));
            }

            User user = (User) authentication.getPrincipal();
            log.info("======================================");
            log.info("Logged in User ID: {}", user.getId());
            log.info("Logged in User Email: {}", user.getEmail());
            log.info("User Authorities: {}", user.getAuthorities());
            log.info("======================================");

            List<PropertyDto> properties = propertyService.getMyProperties(user);
            log.info("Found {} properties for user ID: {}", properties.size(), user.getId());

            return ResponseEntity.ok(ApiResponse.success("Your properties retrieved successfully", properties));
        } catch (Exception e) {
            log.error("Error retrieving user properties", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve your properties"));
        }
    }


    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<PropertyDto>>> searchProperties(
            @RequestBody PropertySearchRequest request) {
        try {
            log.info("Search request received: {}", request);

            Page<PropertyDto> properties = propertyService.searchProperties(request);

            return ResponseEntity.ok(ApiResponse.success(
                    "Properties search completed successfully",
                    properties
            ));
        } catch (Exception e) {
            log.error("Error searching properties", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search properties: " + e.getMessage()));
        }
    }
}
