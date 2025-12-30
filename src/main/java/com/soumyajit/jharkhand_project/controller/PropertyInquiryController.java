package com.soumyajit.jharkhand_project.controller;

import com.soumyajit.jharkhand_project.Response.ApiResponse;
import com.soumyajit.jharkhand_project.dto.CreateInquiryRequest;
import com.soumyajit.jharkhand_project.dto.PropertyInquiryDto;
import com.soumyajit.jharkhand_project.dto.UpdateInquiryStatusRequest;
import com.soumyajit.jharkhand_project.entity.PropertyInquiry;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.service.PropertyInquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PropertyInquiryController {

    private final PropertyInquiryService inquiryService;

    /**
     * ✅ STEP 1: Record button click (instant, no phone)
     * POST /properties/{propertyId}/inquiries/click
     */
    @PostMapping("/properties/{propertyId}/inquiries/click")
    public ResponseEntity<ApiResponse<PropertyInquiryDto>> recordButtonClick(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal User currentUser) {

        log.info("Button click received for property: {}", propertyId);

        PropertyInquiryDto inquiry = inquiryService.recordButtonClick(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Interest recorded successfully", inquiry)
        );
    }

    /**
     * ✅ STEP 2: Submit inquiry with phone (mandatory)
     * POST /properties/{propertyId}/inquiries
     */
    @PostMapping("/properties/{propertyId}/inquiries")
    public ResponseEntity<ApiResponse<PropertyInquiryDto>> submitInquiry(
            @PathVariable Long propertyId,
            @Valid @RequestBody CreateInquiryRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Inquiry submission received for property: {}, phone: {}",
                propertyId, request.getPhoneNumber());

        PropertyInquiryDto inquiry = inquiryService.submitInquiry(propertyId, request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thank you! Our team will contact you soon.", inquiry));
    }

    /**
     * ✅ Get all inquiries (Admin only)
     * GET /admin/inquiries
     */
    @GetMapping("/admin/inquiries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PropertyInquiryDto>>> getAllInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PropertyInquiryDto> inquiries = inquiryService.getAllInquiries(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiries fetched successfully", inquiries)
        );
    }

    /**
     * ✅ Get inquiries by status (Admin only)
     * GET /admin/inquiries/status/{status}
     */
    @GetMapping("/admin/inquiries/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PropertyInquiryDto>>> getInquiriesByStatus(
            @PathVariable PropertyInquiry.InquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PropertyInquiryDto> inquiries = inquiryService.getInquiriesByStatus(status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiries fetched successfully", inquiries)
        );
    }

    /**
     * ✅ Get inquiry counts (Admin only)
     * GET /admin/inquiries/counts
     */
    @GetMapping("/admin/inquiries/counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryCounts>> getInquiryCounts() {

        Long newCount = inquiryService.getNewInquiriesCount();
        Long clickedCount = inquiryService.getClickedInquiriesCount();

        InquiryCounts counts = new InquiryCounts(newCount, clickedCount);

        return ResponseEntity.ok(
                ApiResponse.success("Counts fetched successfully", counts)
        );
    }

    /**
     * ✅ Update inquiry status (Admin only)
     * PUT /admin/inquiries/{inquiryId}/status
     */
    @PutMapping("/admin/inquiries/{inquiryId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyInquiryDto>> updateInquiryStatus(
            @PathVariable Long inquiryId,
            @Valid @RequestBody UpdateInquiryStatusRequest request) {

        PropertyInquiryDto updated = inquiryService.updateInquiryStatus(inquiryId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry status updated successfully", updated)
        );
    }

    /**
     * ✅ Delete inquiry (Admin only)
     * DELETE /admin/inquiries/{inquiryId}
     */
    @DeleteMapping("/admin/inquiries/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long inquiryId) {

        inquiryService.deleteInquiry(inquiryId);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry deleted successfully", null)
        );
    }

    // Helper class for counts
    public record InquiryCounts(Long newCount, Long clickedCount) {}
}
