package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soumyajit.jharkhand_project.entity.PostStatus;
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
public class JobDto {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private String salaryRange;
    private LocalDateTime applicationDeadline;
    private AuthorDto author;
    private List<String> imageUrls;
    private List<CommentDto> comments;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}