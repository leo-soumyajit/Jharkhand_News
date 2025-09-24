package com.soumyajit.jharkhand_project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String token;
    @NotBlank @Size(min = 8)
    private String newPassword;
}
