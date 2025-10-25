package com.soumyajit.jharkhand_project.dto;

import com.soumyajit.jharkhand_project.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String fullName;
    private User.Role role;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public String getFullName() {
        return firstName + " " + lastName;
    }
}
