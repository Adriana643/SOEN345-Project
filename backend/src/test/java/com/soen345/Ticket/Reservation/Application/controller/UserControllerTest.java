package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;
import com.soen345.Ticket.Reservation.Application.repository.UserRepository;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link UserController}.
 * <p>
 * Validates that the user query endpoints correctly distinguish
 * between client and admin users.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanAndSeed() {
        userRepository.deleteAll();
        userService.registerUser(new RegisterRequest("Client1", "c1@test.com", "p", "client"));
        userService.registerUser(new RegisterRequest("Client2", "c2@test.com", "p", "client"));
        userService.registerUser(new RegisterRequest("Admin1", "a1@test.com", "p", "admin"));
    }

    @Test
    @DisplayName("GET /api/users returns all users")
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/users/clients returns only client users")
    void getClients() throws Exception {
        mockMvc.perform(get("/api/users/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("CLIENT"))
                .andExpect(jsonPath("$[1].role").value("CLIENT"));
    }

    @Test
    @DisplayName("GET /api/users/admins returns only admin users")
    void getAdmins() throws Exception {
        mockMvc.perform(get("/api/users/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/role returns correct role info for a client")
    void getUserRoleClient() throws Exception {
        String clientId = userRepository.findByEmail("c1@test.com").orElseThrow().getId();

        mockMvc.perform(get("/api/users/" + clientId + "/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("client"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.isClient").value(true));
    }

    @Test
    @DisplayName("GET /api/users/{id}/role returns correct role info for an admin")
    void getUserRoleAdmin() throws Exception {
        String adminId = userRepository.findByEmail("a1@test.com").orElseThrow().getId();

        mockMvc.perform(get("/api/users/" + adminId + "/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("admin"))
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.isClient").value(false));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 404 for non-existent user")
    void getUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns user details")
    void getUserById() throws Exception {
        String clientId = userRepository.findByEmail("c1@test.com").orElseThrow().getId();

        mockMvc.perform(get("/api/users/" + clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Client1"))
                .andExpect(jsonPath("$.email").value("c1@test.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }
}
