package com.recruitment.platform.service;

import com.recruitment.platform.dto.RegisterRequest;
import com.recruitment.platform.dto.UserResponse;
import com.recruitment.platform.exception.EmailAlreadyExistsException;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String sanitizedEmail = request.getEmail().trim().toLowerCase();

        // Check for duplicate account
        if (userRepository.existsByEmail(sanitizedEmail)) {
            throw new EmailAlreadyExistsException("Account already exists with email: " + sanitizedEmail);
        }

        // Prevent unauthorized privilege escalation to ADMIN
        Role assignedRole = request.getRole() != null ? request.getRole() : Role.CANDIDATE;
        if (assignedRole == Role.ADMIN) {
            throw new IllegalArgumentException("Self-registration as ADMIN is strictly prohibited");
        }

        // Secure password hashing with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
            sanitizedEmail,
            request.getFullName().trim(),
            hashedPassword,
            assignedRole
        );

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }
}
