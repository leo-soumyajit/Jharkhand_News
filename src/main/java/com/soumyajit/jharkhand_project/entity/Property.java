package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus propertyStatus;

    @Column(nullable = false)
    private BigDecimal price;

    private Boolean negotiable = false;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String city;

    private String state;

    private String pincode;

    private String locality;

    @Column(length = 1000)
    private String mapLink;

    private Double latitude;

    private Double longitude;

    private Integer totalArea;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer totalFloors;

    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    private FurnishingStatus furnishingStatus;

    private Boolean parkingAvailable = false;

    private Integer parkingSpaces;

    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    private Integer propertyAge;

    @Enumerated(EnumType.STRING)
    private FacingDirection facingDirection;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    @ElementCollection
    @CollectionTable(name = "property_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    // ✅ ADD THIS - Cloudinary public IDs for property images
    @ElementCollection
    @CollectionTable(name = "property_image_public_ids", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "public_id")
    private List<String> imagePublicIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "property_floor_plans", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "floor_plan_url", length = 500)
    private List<String> floorPlanUrls = new ArrayList<>();

    // ✅ ADD THIS - Cloudinary public IDs for floor plans
    @ElementCollection
    @CollectionTable(name = "property_floor_plan_public_ids", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "public_id")
    private List<String> floorPlanPublicIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    @Enumerated(EnumType.STRING)
    private PostedByType postedByType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PENDING;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PropertyType {
        LAND, FLAT, HOUSE, APARTMENT, VILLA, COMMERCIAL, PLOT
    }

    public enum PropertyStatus {
        FOR_SALE, FOR_RENT, SOLD, RENTED
    }

    public enum FurnishingStatus {
        FURNISHED, SEMI_FURNISHED, UNFURNISHED, FULLY_FURNISHED
    }

    public enum FacingDirection {
        NORTH, SOUTH, EAST, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
    }

    public enum AvailabilityStatus {
        READY_TO_MOVE, UNDER_CONSTRUCTION
    }

    public enum PostedByType {
        OWNER, DEALER, BUILDER, AGENT
    }
}
