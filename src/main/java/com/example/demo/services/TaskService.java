package com.example.demo.services;

import com.example.demo.dto.CommentResponseDTO;
import com.example.demo.dto.TaskRequestDTO;
import com.example.demo.dto.CommentRequestDTO;
import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.entities.*;
import com.example.demo.exceptions.*;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.TaskRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.specifications.TaskSpecifications;
import com.example.demo.utils.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    public TaskMapper getTaskMapper() {
        return taskMapper;
    }

    /**
     * Update status for an existing task by both admin and users.
     */
    public Task updateTaskStatus(Long taskId, TaskStatus status, User user, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (isAdmin) {
            if (user != null && !Role.ROLE_ADMIN.equals(user.getRole())) {
                throw new UnauthorizedActionException("Only admins can update task status globally");
            }
        } else {
            if (task.getAssignee() == null || !task.getAssignee().equals(user) || !Role.ROLE_USER.equals(user.getRole())) {
                throw new UnauthorizedActionException("Only the assigned user can update this task's status");
            }
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }

    // ----------------------------- USER SERVICE -----------------------------

    /**
     * Add a comment to a task.
     */
    public Comment addCommentToTask(Long taskId, CommentRequestDTO request, User loggedInUser) {
        if (loggedInUser == null || !Role.ROLE_USER.equals(loggedInUser.getRole())) {
            throw new UnauthorizedActionException("Only users can add comments to tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        if (!task.getAssignee().equals(loggedInUser)) {
            throw new UnauthorizedActionException("Only the assigned user can add comments to this task");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setAuthor(loggedInUser);

        return commentRepository.save(comment);
    }

    /**
     * Get comments for all tasks.
     */
    public List<CommentResponseDTO> getCommentsForTask(Long taskId, User loggedInUser) {
        if (loggedInUser == null || !Role.ROLE_USER.equals(loggedInUser.getRole())) {
            throw new UnauthorizedActionException("Only users can fetch comments for tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        return commentRepository.findByTask(task)
                .stream()
                .map(taskMapper::mapToCommentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all tasks assigned to a user.
     */
    public List<TaskResponseDTO> getTasksAssignedToUser(User user) {
        if (user == null || !Role.ROLE_USER.equals(user.getRole())) {
            throw new UnauthorizedActionException("Only users can fetch their assigned tasks");
        }

        return taskRepository.findByAssigneeId(user.getId())
                .stream()
                .map(taskMapper::mapToTaskResponseDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------- ADMIN SERVICE -----------------------------

    /**
     * Update priority for an existing task.
     */
    public Task updateTaskPriority(Long taskId, TaskPriority priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        task.setPriority(priority);
        return taskRepository.save(task);
    }

    /**
     * Create a new task.
     */
    public Task createTask(TaskRequestDTO request, User author) {
        try {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            task.setAuthor(author);

            if (request.getAssigneeId() != null) {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new UserNotFoundException("Assignee not found"));
                task.setAssignee(assignee);
            }

            return taskRepository.save(task);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid priority or status value");
        }
    }


    /**
     * Update an existing task.
     */
    public Task updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " not found"));

        try {
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));

            if (request.getAssigneeId() != null) {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new UserNotFoundException("Assignee with ID " + request.getAssigneeId() + " not found"));
                task.setAssignee(assignee);
            }

            return taskRepository.save(task);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid priority or status value");
        }
    }

    /**
     * Delete a task.
     */
    public void deleteTask(Long id) {
        // Ensure the task exists
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " not found"));

        // Delete the task
        taskRepository.delete(task);
    }

    /**
     * Retrieve all tasks with optional pagination.
     */
    public Page<TaskResponseDTO> getAllTasks(String authorEmail, String assigneeEmail, TaskStatus status, TaskPriority priority, Pageable pageable) {
        Specification<Task> spec = Specification.where(null);

        if (authorEmail != null && !authorEmail.isBlank()) {
            spec = spec.and(TaskSpecifications.hasAuthor(authorEmail));
        }
        if (assigneeEmail != null && !assigneeEmail.isBlank()) {
            spec = spec.and(TaskSpecifications.hasAssignee(assigneeEmail));
        }
        if (status != null) {
            spec = spec.and(TaskSpecifications.hasStatus(status));
        }
        if (priority != null) {
            spec = spec.and(TaskSpecifications.hasPriority(priority));
        }

        try {
            Page<Task> tasks = taskRepository.findAll(spec, pageable);
            return tasks.map(taskMapper::mapToTaskResponseDTO);
        } catch (Exception ex) {
            throw new InvalidRequestException("Error occurred while fetching tasks. Please check your filters or pagination parameters.");
        }
    }

}

