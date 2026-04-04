package com.soen345.Ticket.Reservation.Application.dto;

/**
 * Data Transfer Object returned by the authentication endpoints
 * after a successful login or registration.
 * <p>
 * Contains a simple session token, the user's role, email, and
 * database ID so the frontend can persist the session and route
 * the user to the correct home screen (AdminHome vs UserHome).
 * </p>
 */
public class AuthResponse {

    /** A simple session token (UUID-based for this demo). */
    private String token;

    /** The user's role – {@code "admin"} or {@code "client"}. */
    private String role;

    /** The user's email address. */
    private String email;

    /** The user's database ID. */
    private String id;

    /* ── Constructors ─────────────────────────────────────────── */

    public AuthResponse() {
    }

    public AuthResponse(String token, String role, String email, String id) {
        this.token = token;
        this.role = role;
        this.email = email;
        this.id = id;
    }

    /* ── Getters & Setters ────────────────────────────────────── */

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
