package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 * <p>
 * Validates registration logic (creating users as client or admin)
 * and login logic (distinguishing user vs admin requests).
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private String toJson(String name, String email, String password, String role) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email
                + "\",\"password\":\"" + password + "\",\"role\":\"" + role + "\"}";
    }

    private String loginJson(String email, String password, String role) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password
                + "\",\"role\":\"" + role + "\"}";
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register a client and return 201 with auth response")
        void registerClient() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson("Alice", "alice@test.com", "pass123", "client")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(jsonPath("$.role").value("client"))
                    .andExpect(jsonPath("$.email").value("alice@test.com"))
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("Should register an admin and return 201 with auth response")
        void registerAdmin() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson("Bob", "bob@test.com", "admin123", "admin")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("admin"))
                    .andExpect(jsonPath("$.email").value("bob@test.com"));
        }

        @Test
        @DisplayName("Should return 400 when registering with duplicate email")
        void registerDuplicate() throws Exception {
            String body = toJson("Alice", "alice@test.com", "pass", "client");

            // First registration – should succeed
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            // Second registration with same email – should fail
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("An account with this email already exists."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @BeforeEach
        void seedUsers() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson("Client", "client@test.com", "clientpass", "client")));
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson("Admin", "admin@test.com", "adminpass", "admin")));
        }

        @Test
        @DisplayName("Should login client with correct credentials")
        void loginClient() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("client@test.com", "clientpass", "client")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(jsonPath("$.role").value("client"))
                    .andExpect(jsonPath("$.email").value("client@test.com"));
        }

        @Test
        @DisplayName("Should login admin with correct credentials")
        void loginAdmin() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("admin@test.com", "adminpass", "admin")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("admin"));
        }

        @Test
        @DisplayName("Should return 401 for wrong password")
        void loginWrongPassword() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("client@test.com", "wrong", "client")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password."));
        }

        @Test
        @DisplayName("Should return 401 for non-existent email")
        void loginNonExistent() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("noone@test.com", "pass", "client")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password."));
        }

        @Test
        @DisplayName("Should return 401 when client tries to login as admin (role mismatch)")
        void loginRoleMismatch() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson("client@test.com", "clientpass", "admin")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
