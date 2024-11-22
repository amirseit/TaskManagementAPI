package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PriorityUpdateDTO {
    @NotBlank(message = "Priority is required")
    private String priority;
}
