package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.Property.AvailabilityStatus;
import com.soumyajit.jharkhand_project.entity.Property.FurnishingStatus;
import com.soumyajit.jharkhand_project.entity.Property.PostedByType;
import com.soumyajit.jharkhand_project.entity.Property.PropertyStatus;
import com.soumyajit.jharkhand_project.entity.Property.PropertyType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertySearchRequest {


    private String searchQuery;


    private String district;
    private String city;
    private String locality;
    private String state;


    private PropertyStatus propertyStatus;
    private PropertyType propertyType;


    private BigDecimal minPrice;
    private BigDecimal maxPrice;


    private Integer minArea;
    private Integer maxArea;


    private Integer bedrooms;
    private Integer bathrooms;


    private FurnishingStatus furnishingStatus;

    private PostedByType postedByType;


    private AvailabilityStatus availabilityStatus;


    private Boolean parkingAvailable;

    private List<String> amenities;


    private String sortBy = "createdAt";
    private String sortOrder = "desc";

    // PAGINATION
    private Integer page = 0;
    private Integer size = 20;
}
