package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soumyajit.jharkhand_project.entity.NewsCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStateNewsRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @NotBlank(message = "State name is required")
    private String stateName;

//    private String category; // Optional: NEWS, SPORTS, POLITICS, etc.

    // ✅ NEW: Optional category
    private NewsCategory category;  // Optional - can be null
}
