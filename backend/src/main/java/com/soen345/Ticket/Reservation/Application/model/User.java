package com.soen345.Ticket.Reservation.Application.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a registered user in the ticket reservation system.
 * <p>
 * Each user has a unique email and is assigned a {@link Role} that determines
 * whether they are a regular client or an administrator.  The role drives the
 * logic that distinguishes a client request from an admin request across the
 * application.
 * </p>
 *
 * <p><b>Table:</b> {@code users}</p>
 */
@Entity
@Table(name = "users")
public class User {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full display name of the user. */
    @Column(nullable = false)
    private String name;

    /** Unique email address used for authentication. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Hashed password (plain-text storage for demo; production should use BCrypt). */
    @Column(nullable = false)
    private String password;

    /**
     * The user's role – either {@link Role#CLIENT} or {@link Role#ADMIN}.
     * <p>
     * Stored as a string in the database via {@link EnumType#STRING} for
     * readability and forward-compatibility.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /* ── Constructors ─────────────────────────────────────────── */

    /** Default no-arg constructor required by JPA. */
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
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /* ── Getters & Setters ────────────────────────────────────── */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Checks whether this user holds the {@link Role#ADMIN} role.
     *
     * @return {@code true} if the user is an admin, {@code false} otherwise
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Checks whether this user holds the {@link Role#CLIENT} role.
     *
     * @return {@code true} if the user is a client, {@code false} otherwise
     */
    public boolean isClient() {
        return this.role == Role.CLIENT;
    }
}
