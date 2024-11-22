package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        return new ResponseEntity<>(errorDetails, status);
    }

    // Handler for AuthenticationException
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    // Handler for TaskNotFoundException
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Object> handleTaskNotFoundException(TaskNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    // Handler for CommentNotFoundException
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Object> handleCommentNotFoundException(CommentNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    // Handler for UnauthorizedActionException
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Object> handleUnauthorizedActionException(UnauthorizedActionException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }

    // Handler for EmailAlreadyInUseException
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Object> handleEmailAlreadyInUseException(EmailAlreadyInUseException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    // Handler for InvalidRequestException
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> handleInvalidRequestException(InvalidRequestException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    // Handler for UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    // Handler for ValidationException
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    // Handler for AuthorNotFoundException
    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<Object> handleAuthorNotFoundException(AuthorNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    // Generic Exception Handler for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
