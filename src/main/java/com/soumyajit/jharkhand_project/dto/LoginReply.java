package com.soumyajit.jharkhand_project.dto;

public class LoginReply {
    private String token;
    private String role;

    // constructor, getters
    public LoginReply(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
}
