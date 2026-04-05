package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.model.Role;
import com.soen345.Ticket.Reservation.Application.model.User;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserController}.
 * Uses plain Mockito — no Spring context, no Firebase, CI-safe.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User client1;
    private User client2;
    private User admin1;

    @BeforeEach
    void setUp() {
        client1 = new User("c1-id", "Client1", "c1@test.com", "p", Role.CLIENT);
        client2 = new User("c2-id", "Client2", "c2@test.com", "p", Role.CLIENT);
        admin1  = new User("a1-id", "Admin1",  "a1@test.com", "p", Role.ADMIN);
    }

    @Test
    @DisplayName("GET /api/users returns all users")
    void getAllUsers() {

        when(userService.getUserIdFromToken("Bearer valid-token")).thenReturn("a1-id");
        when(userService.isAdmin("a1-id")).thenReturn(true);

        when(userService.getAllUsers()).thenReturn(List.of(client1, client2, admin1));

        ResponseEntity<?> response = userController.getAllUsers("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3,((List<?>) response.getBody()).size());
    }

    @Test
    @DisplayName("GET /api/users/clients returns only client users")
    void getClients() {
        when(userService.getUserIdFromToken("Bearer valid-token")).thenReturn("a1-id");
        when(userService.isAdmin("a1-id")).thenReturn(true);

        when(userService.getAllClients()).thenReturn(List.of(client1, client2));

        ResponseEntity<?> response = userController.getClients("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> body = (List<User>) response.getBody();
        assertEquals(2, body.size());
        assertTrue(body.stream().allMatch(User::isClient));
    }

    @Test
    @DisplayName("GET /api/users/admins returns only admin users")
    void getAdmins() {
        when(userService.getUserIdFromToken("Bearer valid-token")).thenReturn("a1-id");
        when(userService.isAdmin("a1-id")).thenReturn(true);

        when(userService.getAllAdmins()).thenReturn(List.of(admin1));

        ResponseEntity<?> response = userController.getAdmins("Bearer valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<User> body = (List<User>) response.getBody();

        assertEquals(1, body.size());
        assertTrue(body.get(0).isAdmin());
    }

    @Test
    @DisplayName("GET /api/users/{id}/role returns correct role info for a client")
    void getUserRoleClient() {
        when(userService.getUserById("c1-id")).thenReturn(client1);

        ResponseEntity<?> response = userController.getUserRole("c1-id");
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("client", body.get("role"));
        assertEquals(false, body.get("isAdmin"));
        assertEquals(true,  body.get("isClient"));
        assertEquals("c1-id", body.get("id"));
    }

    @Test
    @DisplayName("GET /api/users/{id}/role returns correct role info for an admin")
    void getUserRoleAdmin() {
        when(userService.getUserById("a1-id")).thenReturn(admin1);

        ResponseEntity<?> response = userController.getUserRole("a1-id");
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("admin", body.get("role"));
        assertEquals(true,  body.get("isAdmin"));
        assertEquals(false, body.get("isClient"));
    }


    @Test
    @DisplayName("GET /api/users/{id} returns 404 for non-existent user")
    void getUserByIdNotFound() {
        when(userService.getUserById("99999"))
                .thenThrow(new IllegalArgumentException("User not found."));

        ResponseEntity<?> response = userController.getUserById("99999");
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", body.get("message"));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns user details")
    void getUserById() {
        when(userService.getUserById("c1-id")).thenReturn(client1);

        ResponseEntity<?> response = userController.getUserById("c1-id");
        User body = (User) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Client1",      body.getName());
        assertEquals("c1@test.com",  body.getEmail());
        assertEquals("CLIENT",       body.getRole());
    }
}
