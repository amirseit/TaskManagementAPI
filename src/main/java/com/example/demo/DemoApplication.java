package com.example.demo;

import com.example.demo.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@Component
class AdminInitializer implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		try {
			// Check if admin already exists
			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				// Create and save admin user
				User admin = new User();
				admin.setEmail("admin@example.com");
				admin.setPassword(passwordEncoder.encode("admin")); // Encode the "admin" password
				admin.setRole(Role.ROLE_ADMIN); // Set role to admin
				userRepository.save(admin);
				System.out.println("Admin user created successfully.");
			} else {
				System.out.println("Admin user already exists.");
			}
		} catch (Exception e) {
			// Log the error and provide feedback
			System.err.println("Error occurred during admin initialization: " + e.getMessage());
			e.printStackTrace(); // For debugging purposes
		}
	}
}
