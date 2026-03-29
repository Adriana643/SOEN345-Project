package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.dto.AuthResponse;
import com.soen345.Ticket.Reservation.Application.dto.LoginRequest;
import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller responsible for authentication endpoints.
 * <p>
 * Handles user <b>registration</b> and <b>login</b>, delegating the
 * business logic to {@link UserService}.
 * </p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code POST /api/auth/register} – creates a new user as CLIENT or ADMIN</li>
 *   <li>{@code POST /api/auth/login}    – authenticates a user and verifies role</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")     // allow React Native requests during development
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* ──────────────────────────────────────────────────────────
     *  POST /api/auth/register
     *  Registration logic: creating a user based on whether
     *  they are a user or an admin.
     * ────────────────────────────────────────────────────────── */

    /**
     * Registers a new user account.
     * <p>
     * The request body must contain {@code name}, {@code email},
     * {@code password}, and {@code role} (either {@code "client"}
     * or {@code "admin"}).  The role determines which type of
     * account is created.
     * </p>
     *
     * @param request the registration data
     * @return 201 Created with {@link AuthResponse}, or 400 on validation errors
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /* ──────────────────────────────────────────────────────────
     *  POST /api/auth/login
     *  User logic: distinguishing a request from a user or admin.
     * ────────────────────────────────────────────────────────── */

    /**
     * Authenticates an existing user.
     * <p>
     * Validates credentials <em>and</em> verifies that the selected
     * role matches the role stored in the database — this is the
     * core of the <b>user logic</b> that distinguishes a client
     * request from an admin request.
     * </p>
     *
     * @param request the login data (email, password, role)
     * @return 200 OK with {@link AuthResponse}, or 401 on authentication failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
