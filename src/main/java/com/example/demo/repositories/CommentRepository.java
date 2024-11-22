package com.example.demo.repositories;

import com.example.demo.entities.Comment;
import com.example.demo.entities.Task;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find all comments for a specific task
    List<Comment> findByTask(Task task);

    // Find all comments by a specific user for a task
    List<Comment> findByTaskAndAuthor(Task task, User author);
}
