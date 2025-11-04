// In: com.soumyajit.jharkhand_project.dto.CreateCommentRequest.java

package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String content;
    private Long parentId;
}
