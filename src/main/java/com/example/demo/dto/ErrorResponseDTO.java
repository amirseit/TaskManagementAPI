package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponseDTO {
    @Schema(description = "Error message", example = "Invalid email or password")
    private String message;

    public ErrorResponseDTO(String message) {
        this.message = message;
    }
}

