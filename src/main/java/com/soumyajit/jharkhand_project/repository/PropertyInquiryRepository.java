package com.soumyajit.jharkhand_project.repository;

import com.soumyajit.jharkhand_project.entity.PropertyInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyInquiryRepository extends JpaRepository<PropertyInquiry, Long> {

    // ✅ Get all inquiries for admin dashboard
    Page<PropertyInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ✅ Get inquiries by status
    Page<PropertyInquiry> findByStatusOrderByCreatedAtDesc(
            PropertyInquiry.InquiryStatus status,
            Pageable pageable
    );

    // ✅ Get inquiries for specific property
    List<PropertyInquiry> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    // ✅ Get user's inquiries
    List<PropertyInquiry> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ Check if user already inquired for this property
    Optional<PropertyInquiry> findByUserIdAndPropertyId(Long userId, Long propertyId);

    // ✅ Check if phone number already inquired for this property (guest check)
    Optional<PropertyInquiry> findByPhoneNumberAndPropertyId(String phoneNumber, Long propertyId);

    // ✅ Get new inquiries count
    Long countByStatus(PropertyInquiry.InquiryStatus status);

    // ✅ Get inquiries by date range
    @Query("SELECT pi FROM PropertyInquiry pi WHERE pi.createdAt BETWEEN :startDate AND :endDate ORDER BY pi.createdAt DESC")
    List<PropertyInquiry> findInquiriesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ✅ Get property owner's inquiries (properties they posted)
    @Query("SELECT pi FROM PropertyInquiry pi WHERE pi.property.author.id = :authorId ORDER BY pi.createdAt DESC")
    List<PropertyInquiry> findByPropertyAuthorId(@Param("authorId") Long authorId);
}
