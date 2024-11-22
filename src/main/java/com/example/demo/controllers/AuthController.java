package com.example.demo.controllers;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.exceptions.*;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utils.JwtUtil;
import com.example.demo.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // Authenticate the user
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Fetch user details from the database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate a JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Return the token (and optionally include the user's role)
        return String.format("Bearer %s", token);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        // Check if the email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use");
        }

        // Create a new user entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        user.setRole(Role.ROLE_USER);

        // Save the user to the database
        userRepository.save(user);

        return "User registered successfully!";
    }
}

