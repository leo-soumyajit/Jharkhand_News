package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soumyajit.jharkhand_project.entity.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateNewsDto {
    private Long id;
    private String title;
    private String content;
    private String stateName;
    private AuthorDto author;
    private List<String> imageUrls;
    private List<CommentDto> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ✅ NEW: Category field
    private NewsCategory category;  // Will be null if not set
}

