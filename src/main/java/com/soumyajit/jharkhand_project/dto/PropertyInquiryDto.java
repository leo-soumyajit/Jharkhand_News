package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.PropertyInquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder  // ✅ Added Builder for easier construction
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInquiryDto {

    private Long id;

    // ✅ User Details (Flat structure - easier for frontend)
    private Long userId;
    private String userName;
    private String userEmail;
    private String phoneNumber;

    // ✅ Property Details (Flat structure)
    private Long propertyId;
    private String propertyTitle;
    private String propertyCity;      // ✅ Added
    private String propertyState;     // ✅ Added
    private String propertyAddress;
    private String propertyImageUrl;

    // ✅ Inquiry Details
    private PropertyInquiry.InquiryStatus status;
    private PropertyInquiry.ActionType actionType;
    private String adminNotes;

    // ✅ Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime contactedAt;
    private LocalDateTime submittedAt;

    // ✅ Property Owner/Contact Details (Nested for clarity)
    private PropertyOwnerDto propertyOwner;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder  // ✅ Added Builder
    public static class PropertyOwnerDto {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String postedByType;
    }
}
