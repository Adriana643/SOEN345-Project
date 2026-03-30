package com.soen345.Ticket.Reservation.Application.model;

/**
 * Enum representing the role assigned to a user in the system.
 * <p>
 * Used to distinguish between regular clients and administrators,
 * enabling role-based access control throughout the application.
 * </p>
 *
 * <ul>
 *   <li>{@link #CLIENT} – A standard user who can browse and join events.</li>
 *   <li>{@link #ADMIN}  – An administrator who can create, manage, and delete events.</li>
 * </ul>
 */
public enum Role {

    /** Standard user role – can view and join events. */
    CLIENT,

    /** Administrator role – can manage (create / delete) events. */
    ADMIN
}
