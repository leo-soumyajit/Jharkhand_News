package com.soumyajit.jharkhand_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    // ✅ FIXED: No default value, service will set it
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType = ActionType.BUTTON_CLICK;

    @Column(length = 1000)
    private String adminNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime contactedAt;
    private LocalDateTime submittedAt;

    // ✅ FIXED: ALL statuses added
    public enum InquiryStatus {
        BUTTON_CLICK,      // Matches DB constraint
        FORM_SUBMITTED,    // Matches DB constraint
        CLICKED,           // Frontend button click
        NEW,               // Fresh form submission
        CONTACTED,         // Admin contacted
        INTERESTED,        // User interested
        NOT_INTERESTED,    // User not interested
        CLOSED,            // Case closed
        CONVERTED,         // Sale completed ✅ ADDED
        SPAM               // Spam
    }

    public enum ActionType {
        BUTTON_CLICK,
        FORM_SUBMITTED
    }
}

