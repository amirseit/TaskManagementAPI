package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
