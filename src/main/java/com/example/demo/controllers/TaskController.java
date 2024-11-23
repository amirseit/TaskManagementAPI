package com.example.demo.controllers;

import com.example.demo.dto.*;
import com.example.demo.entities.*;
import com.example.demo.exceptions.*;
import com.example.demo.services.TaskService;
import com.example.demo.repositories.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Get tasks assigned to the logged-in user",
            description = "Retrieve all tasks that are currently assigned to the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Add a comment to a task",
            description = "Allows the logged-in user to add a comment to a task assigned to them.",
            parameters = @Parameter(name = "id", description = "ID of the task to comment on", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment details",
                    content = @Content(schema = @Schema(implementation = CommentRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comment added successfully",
                            content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task or User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Get all comments for a task",
            description = "Retrieve all comments associated with a specific task.",
            parameters = @Parameter(name = "id", description = "ID of the task", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task or User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Update task status for the logged-in user",
            description = "Allows the logged-in user to update the status of a task assigned to them.",
            parameters = @Parameter(name = "id", description = "ID of the task", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New task status",
                    content = @Content(schema = @Schema(example = "{\"status\": \"IN_PROGRESS\"}"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task status updated successfully",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task or User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Create a new task",
            description = "Allows an admin to create a new task.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task details",
                    content = @Content(schema = @Schema(implementation = TaskRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task created successfully",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Update a task",
            description = "Allows an admin to update an existing task.",
            parameters = @Parameter(name = "id", description = "ID of the task to update", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated task details",
                    content = @Content(schema = @Schema(implementation = TaskRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task updated successfully",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Task not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Delete a task",
            description = "Allows an admin to delete an existing task.",
            parameters = @Parameter(name = "id", description = "ID of the task to delete", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
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
    @Operation(
            summary = "Update the status of any task",
            description = "Allows an admin to update the status of a task. The task ID and new status must be provided.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the task to update", required = true),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body containing the new status for the task",
                    required = true,
                    content = @Content(schema = @Schema(example = "{ \"status\": \"IN_PROGRESS\" }"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task status updated successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid task status provided"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            }
    )
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
    @Operation(
            summary = "Update the priority of a task",
            description = "Allows an admin to update the priority of a task. The task ID and new priority must be provided.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the task to update", required = true),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request body containing the new priority for the task",
                    required = true,
                    content = @Content(schema = @Schema(example = "{ \"priority\": \"HIGH\" }"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task priority updated successfully", content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid task priority provided"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            }
    )
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
    @Operation(
            summary = "Retrieve all tasks with filtering and pagination",
            description = "Allows an admin to retrieve all tasks with optional filters (author email, assignee email, status, and priority). Results can be paginated.",
            parameters = {
                    @Parameter(name = "authorEmail", description = "Email of the task author (optional)", required = false),
                    @Parameter(name = "assigneeEmail", description = "Email of the task assignee (optional)", required = false),
                    @Parameter(name = "status", description = "Status of the task (optional)", required = false),
                    @Parameter(name = "priority", description = "Priority of the task (optional)", required = false),
                    @Parameter(name = "pageable", description = "Pagination information (e.g., page number, size, sort)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
            }
    )
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

