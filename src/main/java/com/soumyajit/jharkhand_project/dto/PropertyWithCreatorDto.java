package com.soumyajit.jharkhand_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyWithCreatorDto {

    // ✅ Property Basic Info
    private Long propertyId;
    private String title;
    private String description;
    private String propertyType;
    private String propertyStatus;
    private BigDecimal price;
    private Boolean negotiable;

    // ✅ Location
    private String address;
    private String district;
    private String city;
    private String state;
    private String pincode;
    private String locality;
    private String mapLink;  // 🆕 ADDED

    // ✅ Property Specs
    private Integer totalArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private String furnishingStatus;
    private Boolean parkingAvailable;
    private Integer parkingSpaces;  // 🆕 ADDED
    private List<String> amenities;
    private Integer totalFloors;  // 🆕 ADDED
    private Integer floorNumber;  // 🆕 ADDED
    private Integer propertyAge;  // 🆕 ADDED
    private String facingDirection;  // 🆕 ADDED
    private String availabilityStatus;  // 🆕 ADDED

    // ✅ Images
    private List<String> imageUrls;
    private String mainImageUrl;
    private List<String> floorPlanUrls;  // 🆕 ADDED

    // ✅ CREATOR INFO (User who posted the listing)
    private CreatorDto creator;

    // ✅ CONTACT INFO (Person to call for property)
    private ContactDto contact;

    // ✅ Status & Meta
    private String approvalStatus;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Nested DTO for Creator
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorDto {
        private Long userId;
        private String name;
        private String email;
        private String role;
    }

    // ✅ Nested DTO for Contact
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactDto {
        private String name;
        private String phone;
        private String email;
        private String postedByType;
    }
}
