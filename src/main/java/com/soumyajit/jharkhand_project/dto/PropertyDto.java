package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.Property.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {

    private Long id;
    private String title;
    private String description;
    private PropertyType propertyType;
    private PropertyStatus propertyStatus;
    private BigDecimal price;
    private Boolean negotiable;


    private String address;
    private String district;
    private String city;
    private String state;
    private String pincode;
    private String locality;
    private String mapLink;
    private Double latitude;
    private Double longitude;


    private Integer totalArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer totalFloors;
    private Integer floorNumber;
    private FurnishingStatus furnishingStatus;
    private Boolean parkingAvailable;
    private Integer parkingSpaces;


    private List<String> amenities;
    private Integer propertyAge;
    private FacingDirection facingDirection;
    private AvailabilityStatus availabilityStatus;


    private List<String> imageUrls;
    private List<String> floorPlanUrls;


    private UserSummaryDto author;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private PostedByType postedByType;

    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private List<CommentDto> comments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String name;
        private String email;
    }
}
