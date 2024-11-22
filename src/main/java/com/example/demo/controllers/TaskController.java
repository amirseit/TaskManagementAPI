package com.example.demo.controllers;

import com.example.demo.dto.CommentRequestDTO;
import com.example.demo.dto.CommentResponseDTO;
import com.example.demo.dto.TaskRequestDTO;
import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.entities.*;
import com.example.demo.exceptions.*;
import com.example.demo.services.TaskService;
import com.example.demo.repositories.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    // ----------------------------- USER ENDPOINTS -----------------------------

    /**
     * Get tasks assigned to the logged-in user.
     */
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<TaskResponseDTO> getAssignedTasks(Principal principal) {
        // Get the logged-in user
        User loggedInUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Return tasks assigned to the user
        return taskService.getTasksAssignedToUser(loggedInUser);
    }

    /**
     * Add a comment to a task assigned to the logged-in user.
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('ROLE_USER')")
    public CommentResponseDTO addCommentToTask(@PathVariable Long id,
                                               @RequestBody @Valid CommentRequestDTO request,
                                               Principal principal) {
        // Get the logged-in user
        User loggedInUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Add the comment to the task
        Comment comment = taskService.addCommentToTask(id, request, loggedInUser);
        return taskService.getTaskMapper().mapToCommentResponseDTO(comment);
    }

    /**
     * Get all comments for a task.
     */
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CommentResponseDTO> getCommentsForTask(@PathVariable Long id, Principal principal) {
        User loggedInUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return taskService.getCommentsForTask(id, loggedInUser);
    }

    /**
     * Update the status of a task assigned to the logged-in user.
     */
    @PatchMapping("/{id}/status/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public TaskResponseDTO updateTaskStatusForUser(@PathVariable Long id,
                                                   @RequestBody Map<String, String> request,
                                                   Principal principal) {
        // Get the logged-in user
        User loggedInUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Extract new status from the request
        TaskStatus newStatus = TaskStatus.valueOf(request.get("status").toUpperCase());

        // Update the task status for the user
        Task updatedTask = taskService.updateTaskStatus(id, newStatus, loggedInUser, false);
        return taskService.getTaskMapper().mapToTaskResponseDTO(updatedTask);
    }

    // ----------------------------- ADMIN ENDPOINTS -----------------------------

    /**
     * Create a new task.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TaskResponseDTO createTask(@RequestBody @Valid TaskRequestDTO request, Principal principal) {
        User author = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found"));

        Task task = taskService.createTask(request, author);
        return taskService.getTaskMapper().mapToTaskResponseDTO(task);
    }

    /**
     * Update a task.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TaskResponseDTO updateTask(@PathVariable Long id, @RequestBody @Valid TaskRequestDTO request) {
        Task updatedTask = taskService.updateTask(id, request);
        if (updatedTask == null) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }
        return taskService.getTaskMapper().mapToTaskResponseDTO(updatedTask);
    }

    /**
     * Delete a task.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
        } catch (RuntimeException ex) {
            // This will handle cases where the task isn't found in the repository
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }
    }

    /**
     * Update the status of any task.
     */
    @PatchMapping("/{id}/status/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TaskResponseDTO updateTaskStatusForAdmin(@PathVariable Long id,
                                                    @RequestBody Map<String, String> request) {
        // Validate that the status exists in TaskStatus enum
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(request.get("status").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRequestException("Invalid or missing task status");
        }

        // Update the task status for the admin
        Task updatedTask = taskService.updateTaskStatus(id, newStatus, null, true);
        if (updatedTask == null) {
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }
        return taskService.getTaskMapper().mapToTaskResponseDTO(updatedTask);
    }

    /**
     * Update the priority of a task.
     */
    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TaskResponseDTO updateTaskPriority(@PathVariable Long id,
                                              @RequestBody Map<String, String> request) {
        try {
            // Extract new priority from the request
            TaskPriority newPriority = TaskPriority.valueOf(request.get("priority").toUpperCase());

            // Update the task priority
            Task updatedTask = taskService.updateTaskPriority(id, newPriority);
            return taskService.getTaskMapper().mapToTaskResponseDTO(updatedTask);
        } catch (IllegalArgumentException ex) {
            // Handle invalid priority
            throw new InvalidRequestException("Invalid priority value provided");
        } catch (TaskNotFoundException ex) {
            // Handle task not found
            throw new TaskNotFoundException("Task with ID " + id + " not found");
        }
    }


    /**
     * Retrieve all tasks with optional filtering and pagination.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<TaskResponseDTO> getAllTasks(
            @RequestParam(required = false) String authorEmail,
            @RequestParam(required = false) String assigneeEmail,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable) {
        try {
            // Directly return the result from the service
            return taskService.getAllTasks(authorEmail, assigneeEmail, status, priority, pageable);
        } catch (IllegalArgumentException ex) {
            // Handle invalid status or priority
            throw new InvalidRequestException("Invalid filter parameter provided");
        } catch (Exception ex) {
            // Catch-all for any unexpected exceptions
            throw new InvalidRequestException("An error occurred while retrieving tasks");
        }
    }
}

