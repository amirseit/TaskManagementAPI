package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDTO {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;
}

