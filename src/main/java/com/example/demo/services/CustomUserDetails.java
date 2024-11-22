package com.example.demo.services;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Map the single role from the User entity to a GrantedAuthority object
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Customize if your app tracks account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Customize if your app tracks account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Customize if your app tracks credential expiration
    }

    @Override
    public boolean isEnabled() {
        return true; // Customize if your app tracks user enable/disable status
    }
}
