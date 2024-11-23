package com.example.demo.controllers;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.exceptions.*;
import com.example.demo.repositories.UserRepository;
import com.example.demo.utils.JwtUtil;
import com.example.demo.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication and registration.")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint for logging in a user.
     *
     * @param request LoginRequestDTO containing email and password.
     * @return Bearer token as a string if authentication is successful.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Log in a user",
            description = "Authenticates a user and returns a JWT token for subsequent API calls.",
            requestBody = @RequestBody(
                    description = "User credentials for login",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(example = "Bearer <JWT_TOKEN>"))),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
    public String login(@RequestBody LoginRequestDTO request) {
        try {
            // Authenticate the user
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

    /**
     * Endpoint for registering a new user.
     *
     * @param request RegisterRequestDTO containing email and password.
     * @return Confirmation message indicating successful registration.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with default role 'USER'.",
            requestBody = @RequestBody(
                    description = "Details for user registration",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(schema = @Schema(example = "User registered successfully!"))),
                    @ApiResponse(responseCode = "400", description = "Email already in use",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
            }
    )
    public String register(@RequestBody RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return "User registered successfully!";
    }
}


