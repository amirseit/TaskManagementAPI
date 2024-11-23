package com.example.demo;

import com.example.demo.dto.CommentRequestDTO;
import com.example.demo.dto.TaskRequestDTO;
import com.example.demo.entities.*;
import com.example.demo.exceptions.TaskNotFoundException;
import com.example.demo.exceptions.UnauthorizedActionException;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.TaskRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.TaskService;
import com.example.demo.utils.TaskMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    public TaskServiceTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateTaskStatus_Success_Admin() {
        // Mock data
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.PENDING);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Call the method
        Task updatedTask = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED, null, true);

        // Assertions
        assertNotNull(updatedTask);
        assertEquals(TaskStatus.COMPLETED, updatedTask.getStatus());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testUpdateTaskStatus_Failure_UnauthorizedUser() {
        // Mock data
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);

        Task task = new Task();
        task.setId(1L);
        task.setAssignee(new User()); // Different user

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Call the method and expect an exception
        assertThrows(UnauthorizedActionException.class, () ->
                taskService.updateTaskStatus(1L, TaskStatus.COMPLETED, user, false));
    }

    @Test
    void testAddCommentToTask_Success() {
        // Mock data
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);

        Task task = new Task();
        task.setId(1L);
        task.setAssignee(user);

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("This is a comment");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the method
        Comment comment = taskService.addCommentToTask(1L, request, user);

        // Assertions
        assertNotNull(comment);
        assertEquals("This is a comment", comment.getContent());
        assertEquals(task, comment.getTask());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testAddCommentToTask_Failure_UnauthorizedUser() {
        // Mock data
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_ADMIN); // Admin trying to add a comment (unauthorized)

        // Call the method and expect an exception
        assertThrows(UnauthorizedActionException.class, () ->
                taskService.addCommentToTask(1L, new CommentRequestDTO(), user));
    }
}
