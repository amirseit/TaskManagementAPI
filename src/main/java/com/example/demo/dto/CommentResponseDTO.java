package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDTO {
    private String content;
    private String authorEmail;
    private LocalDateTime createdAt;
}

