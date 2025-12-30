package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CreateInquiryRequest;
import com.soumyajit.jharkhand_project.dto.PropertyInquiryDto;
import com.soumyajit.jharkhand_project.dto.UpdateInquiryStatusRequest;
import com.soumyajit.jharkhand_project.entity.Property;
import com.soumyajit.jharkhand_project.entity.PropertyInquiry;
import com.soumyajit.jharkhand_project.entity.User;
import com.soumyajit.jharkhand_project.repository.PropertyInquiryRepository;
import com.soumyajit.jharkhand_project.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PropertyInquiryService {

    private final PropertyInquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final JavaMailSender mailSender;

    @Value("${app.inquiry.email:support@jharkhandupdates.com}")
    private String inquiryEmail;

    @Value("${app.base-url:https://jharkhandbiharupdates.com}")
    private String baseUrl;

    @Value("${app.mail.from:updatesjharkhandbihar@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Jharkhand Bihar Updates}")
    private String fromName;

    /**
     * ✅ STEP 1: Record button click (instant, no phone required)
     */
    public PropertyInquiryDto recordButtonClick(Long propertyId, User currentUser) {

        if (currentUser == null) {
            throw new RuntimeException("You must be logged in to inquire about properties");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // ✅ Check if user already has ANY inquiry for this property
        Optional<PropertyInquiry> existing = inquiryRepository
                .findByUserIdAndPropertyId(currentUser.getId(), propertyId);

        if (existing.isPresent()) {
            // Return existing inquiry
            log.info("User already has inquiry for this property: UserID={}, PropertyID={}",
                    currentUser.getId(), propertyId);
            return convertToDto(existing.get());
        }

        // ✅ Create new inquiry with CLICKED status
        PropertyInquiry inquiry = new PropertyInquiry();
        inquiry.setProperty(property);
        inquiry.setUser(currentUser);
        inquiry.setPhoneNumber(null);  // No phone yet
        inquiry.setUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
        inquiry.setUserEmail(currentUser.getEmail());
        inquiry.setStatus(PropertyInquiry.InquiryStatus.CLICKED);
        inquiry.setActionType(PropertyInquiry.ActionType.BUTTON_CLICK);

        PropertyInquiry savedInquiry = inquiryRepository.save(inquiry);

        log.info("Button click recorded: ID={}, PropertyID={}, UserID={}",
                savedInquiry.getId(), propertyId, currentUser.getId());

        // ✅ Send email asynchronously (non-blocking)
        sendButtonClickEmail(savedInquiry);

        return convertToDto(savedInquiry);
    }

    /**
     * ✅ STEP 2: Submit inquiry with phone number (mandatory)
     */
    public PropertyInquiryDto submitInquiry(Long propertyId,
                                            CreateInquiryRequest request,
                                            User currentUser) {

        if (currentUser == null) {
            throw new RuntimeException("You must be logged in to submit inquiry");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // ✅ Find existing inquiry (should exist from button click)
        Optional<PropertyInquiry> existingOpt = inquiryRepository
                .findByUserIdAndPropertyId(currentUser.getId(), propertyId);

        PropertyInquiry inquiry;

        if (existingOpt.isPresent()) {
            // Update existing inquiry
            inquiry = existingOpt.get();

            // Check if already submitted
            if (inquiry.getStatus() != PropertyInquiry.InquiryStatus.CLICKED) {
                throw new RuntimeException("You have already submitted inquiry for this property");
            }

            inquiry.setPhoneNumber(request.getPhoneNumber());
            inquiry.setStatus(PropertyInquiry.InquiryStatus.NEW);
            inquiry.setActionType(PropertyInquiry.ActionType.FORM_SUBMITTED);
            inquiry.setSubmittedAt(LocalDateTime.now());

            log.info("Updated inquiry with phone: ID={}, Phone={}",
                    inquiry.getId(), request.getPhoneNumber());

        } else {
            // Create new inquiry (in case user directly submitted without clicking button)
            inquiry = new PropertyInquiry();
            inquiry.setProperty(property);
            inquiry.setUser(currentUser);
            inquiry.setPhoneNumber(request.getPhoneNumber());
            inquiry.setUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
            inquiry.setUserEmail(currentUser.getEmail());
            inquiry.setStatus(PropertyInquiry.InquiryStatus.NEW);
            inquiry.setActionType(PropertyInquiry.ActionType.FORM_SUBMITTED);
            inquiry.setSubmittedAt(LocalDateTime.now());

            log.info("Created new inquiry with phone: PropertyID={}, Phone={}",
                    propertyId, request.getPhoneNumber());
        }

        PropertyInquiry savedInquiry = inquiryRepository.save(inquiry);

        // ✅ Send email asynchronously (non-blocking)
        sendFormSubmissionEmail(savedInquiry);

        return convertToDto(savedInquiry);
    }

    /**
     * ✅ Send email when user just clicks button (no phone) - ASYNC
     */
    @Async("taskExecutor")
    public void sendButtonClickEmail(PropertyInquiry inquiry) {
        try {
            Property property = inquiry.getProperty();
            User propertyOwner = property.getAuthor();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(inquiryEmail);
            helper.setSubject("⚠️ User Showed Interest (No Phone Yet) - " + property.getTitle());

            String emailContent = buildButtonClickEmailContent(inquiry, property, propertyOwner);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("✅ Button click notification email sent to: {}", inquiryEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send button click notification email: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ Send email when user submits form with phone - ASYNC
     */
    @Async("taskExecutor")
    public void sendFormSubmissionEmail(PropertyInquiry inquiry) {
        try {
            Property property = inquiry.getProperty();
            User propertyOwner = property.getAuthor();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(inquiryEmail);
            helper.setSubject("🔔 New Property Inquiry (With Phone) - " + property.getTitle());

            String emailContent = buildFormSubmissionEmailContent(inquiry, property, propertyOwner);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("✅ Form submission notification email sent to: {}", inquiryEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send form submission notification email: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ Email content for button click (no phone)
     */
    private String buildButtonClickEmailContent(PropertyInquiry inquiry,
                                                Property property,
                                                User propertyOwner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .section { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; 
                              border-left: 4px solid #f59e0b; }
                    .section-title { color: #f59e0b; font-weight: bold; margin-bottom: 10px; 
                                    font-size: 18px; }
                    .detail-row { display: flex; margin: 8px 0; }
                    .detail-label { font-weight: bold; min-width: 150px; color: #6b7280; }
                    .detail-value { color: #111827; }
                    .warning { background: #fef3c7; color: #92400e; padding: 15px; 
                              border-radius: 8px; margin: 15px 0; border-left: 4px solid #f59e0b; }
                    .property-link { display: inline-block; background: #f59e0b; color: white; 
                                    padding: 12px 24px; text-decoration: none; border-radius: 6px; 
                                    margin-top: 20px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; margin-top: 30px; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ User Showed Interest</h1>
                        <p>User clicked "Get In Touch" but hasn't provided phone yet</p>
                    </div>
                    
                    <div class="content">
                        <div class="warning">
                            <strong>⚠️ ACTION REQUIRED:</strong> User showed interest but didn't submit phone number yet. 
                            Send an email to manipulate and get their contact number!
                        </div>

                        <!-- User Details -->
                        <div class="section">
                            <div class="section-title">👤 Interested User</div>
                            <div class="detail-row">
                                <span class="detail-label">Name:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Email:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Phone:</span>
                                <span class="detail-value" style="color: #dc2626;">
                                    ⚠️ Not Provided Yet
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Action:</span>
                                <span class="detail-value">Clicked "Get In Touch" button</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>

                        <!-- Property Details -->
                        <div class="section">
                            <div class="section-title">🏠 Property Details</div>
                            <div class="detail-row">
                                <span class="detail-label">Property ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Title:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Type:</span>
                                <span class="detail-value">%s - %s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Price:</span>
                                <span class="detail-value"><strong>₹%s</strong></span>
                            </div>
                            <a href="%s/properties/%d" class="property-link">
                                📍 View Property
                            </a>
                        </div>

                        <!-- Property Owner -->
                        <div class="section">
                            <div class="section-title">📞 Property Owner/Agent</div>
                            <div class="detail-row">
                                <span class="detail-label">Name:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Phone:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Email:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>

                        <!-- Recommended Actions -->
                        <div class="section" style="border-left-color: #dc2626;">
                            <div class="section-title" style="color: #dc2626;">🎯 Recommended Actions</div>
                            <ol style="margin: 10px 0; padding-left: 20px;">
                                <li><strong>Send Email to %s</strong><br>
                                    "Hi %s, we noticed you're interested in '%s'. 
                                    This property is in high demand! Share your phone number 
                                    for instant updates and site visit scheduling."
                                </li>
                                <li>Follow up within 24 hours if no response</li>
                                <li>Update status in admin dashboard</li>
                            </ol>
                        </div>

                        <div class="footer">
                            <p>Jharkhand Bihar Updates - Lead Management System</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                inquiry.getUserName(),
                inquiry.getUserEmail(),
                inquiry.getCreatedAt().format(formatter),
                property.getId(),
                property.getTitle(),
                property.getPropertyType(),
                property.getPropertyStatus(),
                formatPrice(property.getPrice()),
                baseUrl,
                property.getId(),
                property.getContactName() != null ? property.getContactName() :
                        (propertyOwner.getFirstName() + " " + propertyOwner.getLastName()),
                property.getContactPhone() != null ? property.getContactPhone() : "N/A",
                property.getContactEmail() != null ? property.getContactEmail() : propertyOwner.getEmail(),
                inquiry.getUserEmail(),
                inquiry.getUserName().split(" ")[0],
                property.getTitle()
        );
    }

    /**
     * ✅ Email content for form submission (with phone)
     */
    private String buildFormSubmissionEmailContent(PropertyInquiry inquiry,
                                                   Property property,
                                                   User propertyOwner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .section { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; 
                              border-left: 4px solid #10b981; }
                    .section-title { color: #10b981; font-weight: bold; margin-bottom: 10px; 
                                    font-size: 18px; }
                    .detail-row { display: flex; margin: 8px 0; }
                    .detail-label { font-weight: bold; min-width: 150px; color: #6b7280; }
                    .detail-value { color: #111827; }
                    .success { background: #d1fae5; color: #065f46; padding: 15px; 
                              border-radius: 8px; margin: 15px 0; border-left: 4px solid #10b981; }
                    .property-link { display: inline-block; background: #10b981; color: white; 
                                    padding: 12px 24px; text-decoration: none; border-radius: 6px; 
                                    margin-top: 20px; font-weight: bold; }
                    .footer { text-align: center; color: #6b7280; margin-top: 30px; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 New Property Inquiry!</h1>
                        <p>User submitted inquiry with phone number</p>
                    </div>
                    
                    <div class="content">
                        <div class="success">
                            <strong>✅ PHONE NUMBER PROVIDED!</strong> User has submitted full inquiry. 
                            Call them immediately for best conversion!
                        </div>

                        <!-- User Details -->
                        <div class="section">
                            <div class="section-title">👤 Interested User Details</div>
                            <div class="detail-row">
                                <span class="detail-label">Name:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Email:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Phone:</span>
                                <span class="detail-value" style="color: #10b981; font-size: 18px;">
                                    <strong>📞 %s</strong>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Submitted:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>

                        <!-- Property Details -->
                        <div class="section">
                            <div class="section-title">🏠 Property Details</div>
                            <div class="detail-row">
                                <span class="detail-label">Property ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Title:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Type:</span>
                                <span class="detail-value">%s - %s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Price:</span>
                                <span class="detail-value"><strong>₹%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <a href="%s/properties/%d" class="property-link">
                                📍 View Property Details
                            </a>
                        </div>

                        <!-- Property Owner -->
                        <div class="section">
                            <div class="section-title">📞 Property Owner/Agent Contact</div>
                            <div class="detail-row">
                                <span class="detail-label">Name:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Phone:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Email:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Posted By:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>

                        <!-- Action Steps -->
                        <div class="section" style="border-left-color: #ef4444;">
                            <div class="section-title" style="color: #ef4444;">⚡ Immediate Action Required</div>
                            <ol style="margin: 10px 0; padding-left: 20px;">
                                <li><strong>Call %s immediately at %s</strong></li>
                                <li>Understand their requirements and budget</li>
                                <li>Call property owner at %s</li>
                                <li>Connect both parties if interested</li>
                                <li>Schedule site visit if needed</li>
                                <li>Update inquiry status in admin dashboard</li>
                                <li>Track commission if deal closes</li>
                            </ol>
                        </div>

                        <div class="footer">
                            <p>Jharkhand Bihar Updates - Lead Management System</p>
                            <p>Login to dashboard to update inquiry status</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                inquiry.getUserName(),
                inquiry.getUserEmail(),
                inquiry.getPhoneNumber(),
                inquiry.getSubmittedAt() != null ? inquiry.getSubmittedAt().format(formatter) :
                        inquiry.getCreatedAt().format(formatter),
                property.getId(),
                property.getTitle(),
                property.getPropertyType(),
                property.getPropertyStatus(),
                formatPrice(property.getPrice()),
                property.getAddress() + ", " + property.getCity(),
                baseUrl,
                property.getId(),
                property.getContactName() != null ? property.getContactName() :
                        (propertyOwner.getFirstName() + " " + propertyOwner.getLastName()),
                property.getContactPhone() != null ? property.getContactPhone() : "N/A",
                property.getContactEmail() != null ? property.getContactEmail() : propertyOwner.getEmail(),
                property.getPostedByType() != null ? property.getPostedByType().toString() : "N/A",
                inquiry.getUserName().split(" ")[0],
                inquiry.getPhoneNumber(),
                property.getContactPhone() != null ? property.getContactPhone() : "property owner"
        );
    }

    /**
     * ✅ Get all inquiries (Admin only)
     */
    public Page<PropertyInquiryDto> getAllInquiries(Pageable pageable) {
        Page<PropertyInquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc(pageable);
        return inquiries.map(this::convertToDto);
    }

    /**
     * ✅ Get inquiries by status
     */
    public Page<PropertyInquiryDto> getInquiriesByStatus(PropertyInquiry.InquiryStatus status,
                                                         Pageable pageable) {
        Page<PropertyInquiry> inquiries = inquiryRepository
                .findByStatusOrderByCreatedAtDesc(status, pageable);
        return inquiries.map(this::convertToDto);
    }

    /**
     * ✅ Get new inquiries count
     */
    public Long getNewInquiriesCount() {
        return inquiryRepository.countByStatus(PropertyInquiry.InquiryStatus.NEW);
    }

    /**
     * ✅ Get clicked but not submitted count
     */
    public Long getClickedInquiriesCount() {
        return inquiryRepository.countByStatus(PropertyInquiry.InquiryStatus.CLICKED);
    }

    /**
     * ✅ Update inquiry status (Admin only)
     */
    public PropertyInquiryDto updateInquiryStatus(Long inquiryId,
                                                  UpdateInquiryStatusRequest request) {
        PropertyInquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found"));

        inquiry.setStatus(request.getStatus());

        if (request.getAdminNotes() != null) {
            inquiry.setAdminNotes(request.getAdminNotes());
        }

        if (request.getStatus() == PropertyInquiry.InquiryStatus.CONTACTED) {
            inquiry.setContactedAt(LocalDateTime.now());
        }

        PropertyInquiry updated = inquiryRepository.save(inquiry);
        log.info("Inquiry status updated: ID={}, Status={}", inquiryId, request.getStatus());

        return convertToDto(updated);
    }

    /**
     * ✅ Delete inquiry (Admin only)
     */
    public void deleteInquiry(Long inquiryId) {
        if (!inquiryRepository.existsById(inquiryId)) {
            throw new EntityNotFoundException("Inquiry not found");
        }
        inquiryRepository.deleteById(inquiryId);
        log.info("Inquiry deleted: ID={}", inquiryId);
    }

    /**
     * ✅ Convert entity to DTO with ALL fields properly populated
     */
    private PropertyInquiryDto convertToDto(PropertyInquiry inquiry) {
        Property property = inquiry.getProperty();
        User user = inquiry.getUser();

        // ✅ Build Property Owner DTO
        PropertyInquiryDto.PropertyOwnerDto ownerDto = PropertyInquiryDto.PropertyOwnerDto.builder()
                .id(property.getAuthor() != null ? property.getAuthor().getId() : null)
                .name(property.getContactName() != null ? property.getContactName() :
                        (property.getAuthor() != null ?
                                property.getAuthor().getFirstName() + " " + property.getAuthor().getLastName() :
                                "N/A"))
                .email(property.getContactEmail() != null ? property.getContactEmail() :
                        (property.getAuthor() != null ? property.getAuthor().getEmail() : null))
                .phone(property.getContactPhone())
                .postedByType(property.getPostedByType() != null ? property.getPostedByType().toString() : null)
                .build();

        // ✅ Build main DTO using Builder pattern
        return PropertyInquiryDto.builder()
                .id(inquiry.getId())
                // User details
                .userId(user != null ? user.getId() : null)
                .userName(inquiry.getUserName())  // ✅ Direct field from entity
                .userEmail(inquiry.getUserEmail()) // ✅ Direct field from entity
                .phoneNumber(inquiry.getPhoneNumber())
                // Property details
                .propertyId(property.getId())
                .propertyTitle(property.getTitle())
                .propertyCity(property.getCity())         // ✅ Added
                .propertyState(property.getState())       // ✅ Added
                .propertyAddress(property.getAddress())
                .propertyImageUrl(property.getImageUrls() != null && !property.getImageUrls().isEmpty() ?
                        property.getImageUrls().get(0) : null)
                // Inquiry details
                .status(inquiry.getStatus())
                .actionType(inquiry.getActionType())
                .adminNotes(inquiry.getAdminNotes())
                // Timestamps
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .contactedAt(inquiry.getContactedAt())
                .submittedAt(inquiry.getSubmittedAt())
                // Property owner
                .propertyOwner(ownerDto)
                .build();
    }


    private String formatPrice(BigDecimal price) {
        if (price == null) return "N/A";
        double priceValue = price.doubleValue();
        if (priceValue >= 10000000) {
            return String.format("%.2f Crore", priceValue / 10000000);
        }
        if (priceValue >= 100000) {
            return String.format("%.2f Lakh", priceValue / 100000);
        }
        return String.format("%.0f", priceValue);
    }
}
