package com.recruitment.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.platform.dto.RegisterRequest;
import com.recruitment.platform.model.Role;
import com.recruitment.platform.model.User;
import com.recruitment.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SuppressWarnings("null")
class AuthRegistrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a candidate and hash password using BCrypt")
    void testSuccessfulCandidateRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "sarah.connor@example.com",
            "Sarah Connor",
            "StrongP@ssw0rd!",
            Role.CANDIDATE
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("sarah.connor@example.com"))
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                // Verify sensitive password is NEVER returned in response
                .andExpect(jsonPath("$.password").doesNotExist());

        // Verify database persistence and password hashing
        User savedUser = userRepository.findByEmail("sarah.connor@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("StrongP@ssw0rd!");
        assertThat(savedUser.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("StrongP@ssw0rd!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Should default role to CANDIDATE when role is omitted in request")
    void testDefaultRoleAssignment() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "default.role@example.com",
            "Default Role User",
            "ValidP@ss123",
            null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CANDIDATE"));

        User savedUser = userRepository.findByEmail("default.role@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo(Role.CANDIDATE);
    }

    @Test
    @DisplayName("Should successfully register recruiter role")
    void testRecruiterRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "recruiter.dave@talent.com",
            "Dave Miller",
            "RecruiterP@ss1!",
            Role.RECRUITER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("RECRUITER"));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email with 409 Conflict")
    void testDuplicateEmailRegistration() throws Exception {
        RegisterRequest initialRequest = new RegisterRequest(
            "duplicate.test@example.com",
            "First User",
            "P@ssword123",
            Role.CANDIDATE
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated());

        // Attempt second registration with same email (different casing)
        RegisterRequest duplicateRequest = new RegisterRequest(
            "DUPLICATE.TEST@example.com",
            "Second User",
            "P@ssword456!",
            Role.CANDIDATE
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should reject weak password with 400 Bad Request")
    void testWeakPasswordValidation() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "weak.pass@example.com",
            "Weak Password User",
            "weak", // Too short, no numbers/specials
            Role.CANDIDATE
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("Should reject invalid email format with 400 Bad Request")
    void testInvalidEmailValidation() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "not-a-valid-email",
            "Invalid Email User",
            "ValidP@ss123!",
            Role.CANDIDATE
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("Should block unauthorized privilege escalation attempt to ADMIN role")
    void testAdminPrivilegeEscalationPrevented() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "hacker@example.com",
            "Privilege Escalation Attempt",
            "HackerP@ss123!",
            Role.ADMIN
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("ADMIN is strictly prohibited")));
    }
}
