package com.soen345.Ticket.Reservation.Application.dto;

/**
 * Data Transfer Object for user registration requests.
 * <p>
 * Carries user details and the desired role from the frontend
 * registration form to the authentication endpoint.  The role
 * determines whether the new account is created as a
 * {@code CLIENT} or an {@code ADMIN}.
 * </p>
 */
public class RegisterRequest {

    /** The user's full display name. */
    private String name;

    /** The user's email address (must be unique in the system). */
    private String email;

    /** The user's chosen password. */
    private String password;

    /**
     * The role selected during registration – either
     * {@code "client"} or {@code "admin"}.
     */
    private String role;

    /* ── Constructors ─────────────────────────────────────────── */

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /* ── Getters & Setters ────────────────────────────────────── */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
