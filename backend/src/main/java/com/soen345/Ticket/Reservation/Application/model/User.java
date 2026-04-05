package com.soen345.Ticket.Reservation.Application.model;

public class User {

    /** Firestore document ID. */
    private String id;

    /** Full display name of the user. */
    private String name;

    /** Unique email address used for authentication. */
    private String email;

    /** Hashed password (plain-text storage for demo; production should use BCrypt). */
    private String password;

    /**
     * The user's role -> stored as a String in Firestore
     */
    private String role;

    /* ── Constructors ─────────────────────────────────────────── */

    /** Default no-arg constructor*/
    public User() {
    }

    /**
     * Convenience constructor for creating a new user with all required fields.
     *
     * @param name     the user's full name
     * @param email    the user's email address
     * @param password the user's password
     * @param role     the role assigned during registration
     */
    public User(String id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role.name();
    }

    /* ── Getters & Setters ────────────────────────────────────── */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    /* Role Helpers */
    public Role getRoleEnum() {
        try {
            return Role.valueOf(this.role);
        } catch (Exception e) {
            return Role.CLIENT;
        }
    }

    /**
     * Checks whether this user holds the {@link Role#ADMIN} role.
     *
     * @return {@code true} if the user is an admin, {@code false} otherwise
     */
    public boolean isAdmin() {
        return Role.ADMIN.name().equals(this.role);
    }

    /**
     * Checks whether this user holds the {@link Role#CLIENT} role.
     *
     * @return {@code true} if the user is a client, {@code false} otherwise
     */
    public boolean isClient() {
        return Role.CLIENT.name().equals(this.role);
    }
}
