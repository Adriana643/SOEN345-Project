package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.dto.AuthResponse;
import com.soen345.Ticket.Reservation.Application.dto.LoginRequest;
import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthController}.
 * Uses plain Mockito — no Spring context, no Firebase, CI-safe.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should register a client and return 201 with auth response")
        void registerClient() {
            when(userService.registerUser(any(RegisterRequest.class)))
                    .thenReturn(new AuthResponse(null, "client", "alice@test.com", "uid-1"));

            ResponseEntity<?> response = authController.register(
                    new RegisterRequest("Alice", "alice@test.com", "pass123", "client"));
            AuthResponse body = (AuthResponse) response.getBody();

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("client",          body.getRole());
            assertEquals("alice@test.com",  body.getEmail());
            assertEquals("uid-1",           body.getId());
            assertNull(body.getToken());
        }

        @Test
        @DisplayName("Should register an admin and return 201 with auth response")
        void registerAdmin() {
            when(userService.registerUser(any(RegisterRequest.class)))
                    .thenReturn(new AuthResponse(null, "admin", "bob@test.com", "uid-2"));

            ResponseEntity<?> response = authController.register(
                    new RegisterRequest("Bob", "bob@test.com", "admin123", "admin"));
            AuthResponse body = (AuthResponse) response.getBody();

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("admin",         body.getRole());
            assertEquals("bob@test.com",  body.getEmail());
        }

        @Test
        @DisplayName("Should return 400 when registering with duplicate email")
        void registerDuplicate() {
            when(userService.registerUser(any(RegisterRequest.class)))
                    .thenThrow(new IllegalArgumentException("An account with this email already exists."));

            ResponseEntity<?> response = authController.register(
                    new RegisterRequest("Alice", "alice@test.com", "pass", "client"));
            Map<String, Object> body = (Map<String, Object>) response.getBody();

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("An account with this email already exists.", body.get("message"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should login client with correct credentials")
        void loginClient() {
            when(userService.loginUser(any(LoginRequest.class)))
                    .thenReturn(new AuthResponse(null, "client", "client@test.com", "uid-1"));

            ResponseEntity<?> response = authController.login(
                    new LoginRequest("client@test.com", "clientpass", "client"));
            AuthResponse body = (AuthResponse) response.getBody();

            assertEquals(HttpStatus.OK,         response.getStatusCode());
            assertEquals("client",              body.getRole());
            assertEquals("client@test.com",     body.getEmail());
            assertNull(body.getToken());
        }

        @Test
        @DisplayName("Should login admin with correct credentials")
        void loginAdmin() {
            when(userService.loginUser(any(LoginRequest.class)))
                    .thenReturn(new AuthResponse(null, "admin", "admin@test.com", "uid-2"));

            ResponseEntity<?> response = authController.login(
                    new LoginRequest("admin@test.com", "adminpass", "admin"));
            AuthResponse body = (AuthResponse) response.getBody();

            assertEquals(HttpStatus.OK,  response.getStatusCode());
            assertEquals("admin",        body.getRole());
        }

        @Test
        @DisplayName("Should return 401 for wrong password")
        void loginWrongPassword() {
            when(userService.loginUser(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("Invalid email or password."));

            ResponseEntity<?> response = authController.login(
                    new LoginRequest("client@test.com", "wrong", "client"));
            Map<String, Object> body = (Map<String, Object>) response.getBody();

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid email or password.", body.get("message"));
        }

        @Test
        @DisplayName("Should return 401 for non-existent email")
        void loginNonExistent() {
            when(userService.loginUser(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("Invalid email or password."));

            ResponseEntity<?> response = authController.login(
                    new LoginRequest("noone@test.com", "pass", "client"));
            Map<String, Object> body = (Map<String, Object>) response.getBody();

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Invalid email or password.", body.get("message"));
        }

        @Test
        @DisplayName("Should return 401 when client tries to login as admin (role mismatch)")
        void loginRoleMismatch() {
            when(userService.loginUser(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException(
                            "This account is registered as a client. Please select the correct role."));

            ResponseEntity<?> response = authController.login(
                    new LoginRequest("client@test.com", "clientpass", "admin"));
            Map<String, Object> body = (Map<String, Object>) response.getBody();

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(body.get("message"));
        }
    }
}