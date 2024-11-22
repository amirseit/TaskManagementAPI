package com.example.demo.repositories;

import com.example.demo.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    // Find tasks by author
    List<Task> findByAuthorId(Long authorId);

    // Find tasks assigned to a specific user
    List<Task> findByAssigneeId(Long assigneeId);

    // Find a task by its ID (Optional for safe handling)
    Optional<Task> findById(Long id);


    @EntityGraph(attributePaths = {"comments", "comments.author"}) // Eager load comments and their authors
    Page<Task> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "comments.author"})
    Page<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);
}