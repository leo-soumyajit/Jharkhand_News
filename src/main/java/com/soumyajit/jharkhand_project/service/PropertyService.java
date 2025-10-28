package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CommentDto;
import com.soumyajit.jharkhand_project.dto.CreatePropertyRequest;
import com.soumyajit.jharkhand_project.dto.PropertyDto;
import com.soumyajit.jharkhand_project.dto.PropertySearchRequest;
import com.soumyajit.jharkhand_project.entity.*;
import com.soumyajit.jharkhand_project.repository.CommentRepository;
import com.soumyajit.jharkhand_project.repository.PropertyRepository;
import com.soumyajit.jharkhand_project.specification.PropertySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;



    @Cacheable(value = "properties", key = "'approved'")
    public List<PropertyDto> getApprovedProperties() {
        List<Property> properties = propertyRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED);

        return properties.stream()
                .filter(p -> p.getPropertyStatus() == Property.PropertyStatus.FOR_SALE ||
                        p.getPropertyStatus() == Property.PropertyStatus.FOR_RENT)
                .map(property -> modelMapper.map(property, PropertyDto.class))
                .collect(Collectors.toList());
    }

    public PropertyDto createProperty(CreatePropertyRequest request,
                                      List<MultipartFile> images,
                                      List<MultipartFile> floorPlans,
                                      User author) {


        if (request.getPropertyStatus() != Property.PropertyStatus.FOR_SALE &&
                request.getPropertyStatus() != Property.PropertyStatus.FOR_RENT) {
            throw new IllegalArgumentException("Property status must be FOR_SALE or FOR_RENT");
        }


        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one property image is required");
        }


        if (images.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 images allowed");
        }


        if (floorPlans != null && floorPlans.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 floor plans allowed");
        }

        Property property = modelMapper.map(request, Property.class);
        property.setAuthor(author);
        property.setStatus(PostStatus.PENDING);
        property.setState("Jharkhand");


        try {
            List<String> imageUrls = cloudinaryService.uploadImages(images);
            property.setImageUrls(imageUrls);
            log.info("Uploaded {} property images", imageUrls.size());
        } catch (Exception e) {
            log.error("Error uploading property images", e);
            throw new RuntimeException("Failed to upload property images: " + e.getMessage());
        }


        if (floorPlans != null && !floorPlans.isEmpty()) {
            try {
                List<String> floorPlanUrls = cloudinaryService.uploadImages(floorPlans);
                property.setFloorPlanUrls(floorPlanUrls);
                log.info("Uploaded {} floor plans", floorPlanUrls.size());
            } catch (Exception e) {
                log.error("Error uploading floor plans", e);
                throw new RuntimeException("Failed to upload floor plans: " + e.getMessage());
            }
        }

        Property savedProperty = propertyRepository.save(property);
        log.info("Created property with ID: {} by user: {}", savedProperty.getId(), author.getEmail());

        return modelMapper.map(savedProperty, PropertyDto.class);
    }

    @CacheEvict(value = {"properties", "recent-properties"}, allEntries = true)
    public PropertyDto approveProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + propertyId));

        property.setStatus(PostStatus.APPROVED);
        Property savedProperty = propertyRepository.save(property);
        log.info("Approved property with ID: {}", propertyId);

        notificationService.notifyUser(property.getAuthor().getId(),
                "Your property '" + property.getTitle() + "' has been approved!");

        return modelMapper.map(savedProperty, PropertyDto.class);
    }

    public List<PropertyDto> getPendingProperties() {
        List<Property> properties = propertyRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
        return properties.stream()
                .map(property -> modelMapper.map(property, PropertyDto.class))
                .collect(Collectors.toList());
    }

    public PropertyDto getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));

        // Increment view count
        property.setViewCount(property.getViewCount() + 1);
        propertyRepository.save(property);

        PropertyDto dto = modelMapper.map(property, PropertyDto.class);


        List<Comment> comments = commentRepository.findByPropertyIdOrderByCreatedAtDesc(id);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());

        dto.setComments(commentDtos);
        return dto;
    }

    @CacheEvict(value = {"properties", "recent-properties"}, allEntries = true)
    public void deleteProperty(Long propertyId, User user) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + propertyId));

        boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
        boolean isOwner = property.getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Unauthorized: You can only delete your own properties");
        }


        commentRepository.deleteByPropertyId(propertyId);


        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            try {
                for (String imageUrl : property.getImageUrls()) {
                    cloudinaryService.deleteImage(imageUrl);
                }
                log.info("Deleted {} images from Cloudinary", property.getImageUrls().size());
            } catch (Exception e) {
                log.warn("Failed to delete images from Cloudinary: {}", e.getMessage());
            }
        }

        if (property.getFloorPlanUrls() != null && !property.getFloorPlanUrls().isEmpty()) {
            try {
                for (String floorPlanUrl : property.getFloorPlanUrls()) {
                    cloudinaryService.deleteImage(floorPlanUrl);
                }
                log.info("Deleted {} floor plans from Cloudinary", property.getFloorPlanUrls().size());
            } catch (Exception e) {
                log.warn("Failed to delete floor plans from Cloudinary: {}", e.getMessage());
            }
        }

        propertyRepository.delete(property);
        log.info("Property deleted with ID: {} by user: {} ({})",
                propertyId, user.getEmail(), isAdmin ? "ADMIN" : "OWNER");
    }

    @CacheEvict(value = {"properties", "recent-properties"}, allEntries = true)
    public PropertyDto updatePropertyStatus(Long propertyId, Property.PropertyStatus newStatus, User user) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + propertyId));

        if (!property.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only update your own properties");
        }


        if (property.getPropertyStatus() == Property.PropertyStatus.FOR_SALE &&
                newStatus != Property.PropertyStatus.SOLD) {
            throw new IllegalArgumentException("FOR_SALE property can only be marked as SOLD");
        }

        if (property.getPropertyStatus() == Property.PropertyStatus.FOR_RENT &&
                newStatus != Property.PropertyStatus.RENTED) {
            throw new IllegalArgumentException("FOR_RENT property can only be marked as RENTED");
        }

        property.setPropertyStatus(newStatus);
        Property savedProperty = propertyRepository.save(property);

        log.info("Property ID: {} status changed to {} by user: {}",
                propertyId, newStatus, user.getEmail());

        return modelMapper.map(savedProperty, PropertyDto.class);
    }

    @Cacheable(value = "recent-properties", key = "#days")
    public List<PropertyDto> getRecentProperties(int days) {
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(days);

        List<Property> recentProperties = propertyRepository
                .findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(PostStatus.APPROVED, dateThreshold);

        return recentProperties.stream()
                .filter(p -> p.getPropertyStatus() == Property.PropertyStatus.FOR_SALE ||
                        p.getPropertyStatus() == Property.PropertyStatus.FOR_RENT)
                .map(property -> modelMapper.map(property, PropertyDto.class))
                .collect(Collectors.toList());
    }

    public List<PropertyDto> getMyProperties(User user) {
        List<Property> properties = propertyRepository.findByAuthorId(user.getId());
        return properties.stream()
                .map(property -> modelMapper.map(property, PropertyDto.class))
                .collect(Collectors.toList());
    }



    public Page<PropertyDto> searchProperties(PropertySearchRequest request) {
        log.info("Searching properties with filters: {}", request);


        Specification<Property> spec = PropertySpecification.filterProperties(request);


        Sort sort = buildSort(request);


        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                sort
        );


        Page<Property> properties = propertyRepository.findAll(spec, pageable);

        log.info("Found {} properties matching criteria", properties.getTotalElements());


        return properties.map(this::convertToDto);
    }

    private PropertyDto convertToDto(Property property) {
        PropertyDto dto = new PropertyDto();

        // Basic Info
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setPropertyType(property.getPropertyType());
        dto.setPropertyStatus(property.getPropertyStatus());
        dto.setPrice(property.getPrice());
        dto.setNegotiable(property.getNegotiable());

        // Location
        dto.setAddress(property.getAddress());
        dto.setDistrict(property.getDistrict());
        dto.setCity(property.getCity());
        dto.setState(property.getState());
        dto.setPincode(property.getPincode());
        dto.setLocality(property.getLocality());
        dto.setMapLink(property.getMapLink());
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());

        // Specifications
        dto.setTotalArea(property.getTotalArea());
        dto.setBedrooms(property.getBedrooms());
        dto.setBathrooms(property.getBathrooms());
        dto.setTotalFloors(property.getTotalFloors());
        dto.setFloorNumber(property.getFloorNumber());
        dto.setFurnishingStatus(property.getFurnishingStatus());
        dto.setParkingAvailable(property.getParkingAvailable());
        dto.setParkingSpaces(property.getParkingSpaces());

        // Additional
        dto.setAmenities(property.getAmenities());
        dto.setPropertyAge(property.getPropertyAge());
        dto.setFacingDirection(property.getFacingDirection());
        dto.setAvailabilityStatus(property.getAvailabilityStatus());

        // Media
        dto.setImageUrls(property.getImageUrls());
        dto.setFloorPlanUrls(property.getFloorPlanUrls());

        // Author
        if (property.getAuthor() != null) {
            PropertyDto.UserSummaryDto authorDto = new PropertyDto.UserSummaryDto();
            authorDto.setId(property.getAuthor().getId());
            String fullName = (property.getAuthor().getFirstName() != null ? property.getAuthor().getFirstName() : "") +
                    " " +
                    (property.getAuthor().getLastName() != null ? property.getAuthor().getLastName() : "");
            authorDto.setName(fullName.trim());
            authorDto.setEmail(property.getAuthor().getEmail());
            dto.setAuthor(authorDto);
        }

        // Contact
        dto.setContactName(property.getContactName());
        dto.setContactPhone(property.getContactPhone());
        dto.setContactEmail(property.getContactEmail());
        dto.setPostedByType(property.getPostedByType());

        // System
        dto.setStatus(property.getStatus().toString());
        dto.setViewCount(property.getViewCount());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());

        dto.setComments(null);

        return dto;
    }

    private Sort buildSort(PropertySearchRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "desc";

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }


}
