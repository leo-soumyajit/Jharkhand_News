package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.Property.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Property status is required")
    private PropertyStatus propertyStatus;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private Boolean negotiable;

    // Location
    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "City is required")
    private String city;

    private String pincode;

    private String locality;


    private String mapLink;

    private Double latitude;

    private Double longitude;


    @NotNull(message = "Total area is required")
    @Min(value = 1, message = "Total area must be at least 1 sq ft")
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


    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact phone must be 10 digits")
    private String contactPhone;

    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotNull(message = "Posted by type is required")
    private PostedByType postedByType;
}
