package com.soumyajit.jharkhand_project.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStateRequest {

    @NotBlank(message = "State name is required")
    private String name;

    private String code; // Optional
}

