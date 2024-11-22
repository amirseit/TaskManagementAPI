package com.example.demo.utils;

import com.example.demo.dto.CommentResponseDTO;
import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.entities.Comment;
import com.example.demo.entities.Task;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TaskMapper {

    /**
     * Map a Task entity to a TaskResponseDTO.
     */
    public TaskResponseDTO mapToTaskResponseDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().name());
        dto.setPriority(task.getPriority().name());
        dto.setAuthorEmail(task.getAuthor().getEmail());
        dto.setAssigneeEmail(task.getAssignee() != null ? task.getAssignee().getEmail() : null);
        dto.setComments(task.getComments()
                .stream()
                .map(this::mapToCommentResponseDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Map a Comment entity to a CommentResponseDTO.
     */
    public CommentResponseDTO mapToCommentResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setContent(comment.getContent());
        dto.setAuthorEmail(comment.getAuthor().getEmail());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
