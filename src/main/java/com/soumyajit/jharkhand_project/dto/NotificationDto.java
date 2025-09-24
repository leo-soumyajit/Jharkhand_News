package com.soumyajit.jharkhand_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class NotificationDto {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
}
