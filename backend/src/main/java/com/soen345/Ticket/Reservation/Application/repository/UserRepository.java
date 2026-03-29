package com.soen345.Ticket.Reservation.Application.repository;

import com.soen345.Ticket.Reservation.Application.model.Role;
import com.soen345.Ticket.Reservation.Application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 * <p>
 * Provides CRUD operations and custom query methods used by
 * the authentication and user-management services.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Used during login to locate the account and during
     * registration to check for duplicates.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if the email is already registered
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves all users that have been assigned the specified role.
     * <p>
     * Useful for admin dashboards that need to list all clients,
     * or to distinguish admin users from regular users.
     * </p>
     *
     * @param role the role to filter by ({@link Role#CLIENT} or {@link Role#ADMIN})
     * @return a list of users with the given role
     */
    List<User> findByRole(Role role);
}
