package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.model.User;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for <b>user-related operations</b>.
 * <p>
 * Implements the <b>user logic</b> that distinguishes requests coming
 * from a regular user versus an administrator by exposing role-aware
 * endpoints.
 * </p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code GET /api/users}            – list all registered users</li>
 *   <li>{@code GET /api/users/{id}}       – get a single user by ID</li>
 *   <li>{@code GET /api/users/{id}/role}  – check a user's role</li>
 *   <li>{@code GET /api/users/admins}     – list all admin users</li>
 *   <li>{@code GET /api/users/clients}    – list all client users</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* ──────────────────────────────────────────────────────────
     *  GET /api/users
     *  Returns every registered user.
     * ────────────────────────────────────────────────────────── */

    /**
     * Lists all users in the system (admin-only in production).
     *
     * @return 200 OK with a list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /* ──────────────────────────────────────────────────────────
     *  GET /api/users/{id}
     *  Returns a single user by their database ID.
     * ────────────────────────────────────────────────────────── */

    /**
     * Retrieves a user by ID.
     *
     * @param id the user's database ID
     * @return 200 OK with user data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /* ──────────────────────────────────────────────────────────
     *  GET /api/users/{id}/role
     *  Distinguishes whether a user is a client or admin.
     * ────────────────────────────────────────────────────────── */

    /**
     * Returns the role of a specific user.
     * <p>
     * This is the primary endpoint for the <b>user logic</b>:
     * the frontend can call this to determine whether a request
     * is coming from a client or an admin and route accordingly.
     * </p>
     *
     * @param id the user's database ID
     * @return 200 OK with role information
     */
    @GetMapping("/{id}/role")
    public ResponseEntity<?> getUserRole(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "role", user.getRoleEnum().name().toLowerCase(),
                    "isAdmin", user.isAdmin(),
                    "isClient", user.isClient()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /* ──────────────────────────────────────────────────────────
     *  GET /api/users/admins
     *  Lists only admin users.
     * ────────────────────────────────────────────────────────── */

    /**
     * Returns all users that hold the ADMIN role.
     *
     * @return 200 OK with a list of admin users
     */
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }

    /* ──────────────────────────────────────────────────────────
     *  GET /api/users/clients
     *  Lists only client users.
     * ────────────────────────────────────────────────────────── */

    /**
     * Returns all users that hold the CLIENT role.
     *
     * @return 200 OK with a list of client users
     */
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getClients() {
        return ResponseEntity.ok(userService.getAllClients());
    }
}
