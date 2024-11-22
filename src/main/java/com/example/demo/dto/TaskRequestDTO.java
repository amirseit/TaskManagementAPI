package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Priority is required")
    private String priority;

    @NotBlank(message = "Status is required")
    private String status;

    private Long assigneeId;
}


