package com.soumyajit.jharkhand_project.specification;

import com.soumyajit.jharkhand_project.dto.PropertySearchRequest;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import com.soumyajit.jharkhand_project.entity.Property;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    public static Specification<Property> filterProperties(PropertySearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.APPROVED));

            // TEXT SEARCH (searches in title AND description)
            if (request.getSearchQuery() != null && !request.getSearchQuery().isEmpty()) {
                String searchPattern = "%" + request.getSearchQuery().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
                ));
            }

            // LOCATION FILTERS (all optional - user chooses one or more)
            if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("district"), request.getDistrict()));
            }

            if (request.getCity() != null && !request.getCity().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("city"), request.getCity()));
            }

            if (request.getLocality() != null && !request.getLocality().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("locality")),
                        "%" + request.getLocality().toLowerCase() + "%"
                ));
            }

            if (request.getState() != null && !request.getState().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("state"), request.getState()));
            }

            // PROPERTY STATUS (optional - FOR_SALE or FOR_RENT)
            if (request.getPropertyStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("propertyStatus"), request.getPropertyStatus()));
            }

            // PROPERTY TYPE (optional - FLAT, HOUSE, etc.)
            if (request.getPropertyType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("propertyType"), request.getPropertyType()));
            }

            // PRICE RANGE (optional)
            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            // AREA RANGE (optional)
            if (request.getMinArea() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalArea"), request.getMinArea()));
            }

            if (request.getMaxArea() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalArea"), request.getMaxArea()));
            }

            // BEDROOMS (optional)
            if (request.getBedrooms() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bedrooms"), request.getBedrooms()));
            }

            // BATHROOMS (optional)
            if (request.getBathrooms() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bathrooms"), request.getBathrooms()));
            }

            // FURNISHING STATUS (optional)
            if (request.getFurnishingStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("furnishingStatus"), request.getFurnishingStatus()));
            }

            // POSTED BY TYPE (optional)
            if (request.getPostedByType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("postedByType"), request.getPostedByType()));
            }

            // AVAILABILITY STATUS (optional)
            if (request.getAvailabilityStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("availabilityStatus"), request.getAvailabilityStatus()));
            }

            // PARKING AVAILABLE (optional)
            if (request.getParkingAvailable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parkingAvailable"), request.getParkingAvailable()));
            }

            // AMENITIES (optional - property must have ALL specified amenities)
            if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
                for (String amenity : request.getAmenities()) {
                    predicates.add(criteriaBuilder.isMember(amenity, root.get("amenities")));
                }
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
