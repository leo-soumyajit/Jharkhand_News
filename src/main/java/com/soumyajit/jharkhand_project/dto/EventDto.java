package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soumyajit.jharkhand_project.entity.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {
    private Long id;
    private String title;
    private String description;
    private String reglink;
    private LocalDateTime eventDate;
    private String location;
    private AuthorDto author;
    private List<String> imageUrls;
    private List<CommentDto> comments;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 🆕 OPTIONAL - Event End Date (DATE ONLY)
    private LocalDate endDate;
}
