package com.example.demo.exceptions;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("status", HttpServletResponse.SC_FORBIDDEN);
        responseBody.put("error", "Forbidden");
        responseBody.put("message", "You do not have permission to perform this action.");
        responseBody.put("path", request.getRequestURI());

        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody));
    }
}