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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.CLICKED;

    // ✅ Track action type
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

    // ✅ Track when form was submitted (if submitted)
    private LocalDateTime submittedAt;

    public enum InquiryStatus {
        CLICKED,           // User clicked button but didn't submit
        NEW,               // User submitted with phone
        CONTACTED,         // Owner called/emailed user
        INTERESTED,        // User confirmed interest
        NOT_INTERESTED,    // User not interested
        CLOSED,         // Deal closed
        SPAM              // Spam inquiry
    }

    public enum ActionType {
        BUTTON_CLICK,      // Just clicked "Get In Touch"
        FORM_SUBMITTED     // Submitted form with phone
    }
}
