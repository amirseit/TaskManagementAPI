package com.example.demo.specifications;

import com.example.demo.entities.Task;
import com.example.demo.entities.TaskPriority;
import com.example.demo.entities.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {
    public static Specification<Task> hasAuthor(String authorEmail) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("author").get("email"), authorEmail);
    }

    public static Specification<Task> hasAssignee(String assigneeEmail) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assignee").get("email"), assigneeEmail);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority);
    }
}

