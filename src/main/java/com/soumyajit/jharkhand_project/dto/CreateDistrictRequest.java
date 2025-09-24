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
public class CreateDistrictRequest {

    @NotBlank(message = "District name is required")
    private String name;

    private String code; // Optional
}

