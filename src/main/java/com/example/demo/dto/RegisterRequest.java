package com.example.demo.dto;

import com.example.demo.entities.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Set<Role> roles; // Optional
}

