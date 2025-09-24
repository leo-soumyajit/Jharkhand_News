package com.soumyajit.jharkhand_project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soumyajit.jharkhand_project.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private User.Role role;
}
