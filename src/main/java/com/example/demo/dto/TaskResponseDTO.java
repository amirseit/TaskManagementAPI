package com.example.demo.dto;

import lombok.Data;
import com.example.demo.entities.Task;

import java.util.List;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String authorEmail;
    private String assigneeEmail;
    private List<CommentResponseDTO> comments;
}
