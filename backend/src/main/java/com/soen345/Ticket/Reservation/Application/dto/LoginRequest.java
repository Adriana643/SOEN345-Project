package com.soen345.Ticket.Reservation.Application.dto;

/**
 * Data Transfer Object for user login requests.
 * <p>
 * Carries the credentials and the selected role from the frontend
 * login form to the authentication endpoint.
 * </p>
 */
public class LoginRequest {

    /** The user's email address. */
    private String email;

    /** The user's password. */
    private String password;

    /**
     * The role the user is attempting to log in as
     * (must match the role stored in the database).
     */
    private String role;

    /* ── Constructors ─────────────────────────────────────────── */

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /* ── Getters & Setters ────────────────────────────────────── */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
