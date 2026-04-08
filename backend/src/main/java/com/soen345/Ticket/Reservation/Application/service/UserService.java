package com.soen345.Ticket.Reservation.Application.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.soen345.Ticket.Reservation.Application.config.FirebaseTokenVerifier;
import com.soen345.Ticket.Reservation.Application.dto.AuthResponse;
import com.soen345.Ticket.Reservation.Application.dto.FirebaseAuthRequest;
import com.soen345.Ticket.Reservation.Application.dto.LoginRequest;
import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;

import com.soen345.Ticket.Reservation.Application.model.Role;
import com.soen345.Ticket.Reservation.Application.model.User;
import com.soen345.Ticket.Reservation.Application.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer containing the core business logic for
 * <strong>user management</strong>, <strong>registration</strong>,
 * and <strong>login</strong>.
 * <p>
 * This service is responsible for:
 * <ul>
 *   <li><b>Registration logic</b> – creating a user based on whether they
 *       selected the "client" or "admin" role during sign-up.</li>
 *   <li><b>User logic</b> – distinguishing requests from regular users
 *       vs. administrators so the correct permissions and views are applied.</li>
 * </ul>
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    /**
     * Constructor-based dependency injection.
     *
     * @param userRepository the repository used for user persistence
     */

    public UserService(UserRepository userRepository,  FirebaseTokenVerifier firebaseTokenVerifier) {
        this.userRepository = userRepository;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
    }

    public AuthResponse firebaseAuth(FirebaseAuthRequest request) throws Exception {
        // verify the token Firebase issued on the frontend
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(request.getIdToken());

        String firebaseUid = decoded.getUid();
        String email = decoded.getEmail();
        String phone = (String) decoded.getClaims().get("phone_number");
        String identifier = email != null ? email : phone;

        Role role = request.getRole().equalsIgnoreCase("admin") ? Role.ADMIN : Role.CLIENT;

        User user = userRepository.findByFirebaseUid(firebaseUid).orElseGet(() -> {
            User u = new User();
            u.setFirebaseUid(firebaseUid);
            u.setEmail(email);
            u.setPhone(phone);
            u.setName(request.getName());
            u.setRole(role.name()); // ← fixed
            return userRepository.save(u);
        });

        return new AuthResponse(firebaseUid, user.getRole().toLowerCase(), identifier, user.getId());
    }


    /* ═══════════════════════════════════════════════════════════
     *  REGISTRATION LOGIC
     *  Creates a user based on whether they are a user or admin.
     * ═══════════════════════════════════════════════════════════ */

    /**
     * Registers a new user in the system.
     * <p>
     * The role string received from the frontend (e.g. {@code "admin"} or
     * {@code "client"}) is normalised to the {@link Role} enum so the user
     * is persisted with the correct role.  Duplicate emails are rejected.
     * </p>
     *
     * @param request the registration payload containing name, email, password, and role
     * @return an {@link AuthResponse} with a generated session token and user details
     * @throws IllegalArgumentException if the email is already registered
     */
    public AuthResponse registerUser(RegisterRequest request) {
        // Prevent duplicate accounts
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String newId = (request.getFirebaseUid() != null && !request.getFirebaseUid().isBlank())
                ? request.getFirebaseUid()
                : UUID.randomUUID().toString();

        // Determine the role based on the selection from the frontend
        Role role = resolveRole(request.getRole());

        // Use Firebase UID as the document ID so token-based lookups always match
        User user = new User(
                request.getFirebaseUid(),
                request.getName(),
                request.getEmail(),
                request.getPassword(),   // NOTE: plain-text for demo; use BCrypt in production
                role);

        user = userRepository.save(user);
        // Build and return the auth response
        return buildAuthResponse(user);
    }

    /* ═══════════════════════════════════════════════════════════
     *  LOGIN / USER LOGIC
     *  Distinguishes a request from a user or admin.
     * ═══════════════════════════════════════════════════════════ */

    /**
     * Authenticates a user and verifies that their stored role matches
     * the role they attempt to log in with.
     * <p>
     * This is the key piece of <b>user logic</b>: a client cannot log in
     * through the admin portal and vice-versa, ensuring that requests
     * from users and admins are properly distinguished.
     * </p>
     *
     * @param request the login payload containing email, password, and role
     * @return an {@link AuthResponse} on successful authentication
     * @throws IllegalArgumentException if credentials are invalid or roles do not match
     */
    public AuthResponse loginUser(LoginRequest request) {
        // Look up the user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        // Verify password (plain-text comparison for demo purposes)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Distinguish between user and admin – the requested role must match the stored role
        Role requestedRole = resolveRole(request.getRole());
        if (user.getRoleEnum() != requestedRole) {
            throw new IllegalArgumentException(
                    "This account is registered as a "
                            + user.getRoleEnum().name().toLowerCase()
                            + ". Please select the correct role."
            );
        }

        return buildAuthResponse(user);
    }

    /* ═══════════════════════════════════════════════════════════
     *  TOKEN RESOLUTION
     * ═══════════════════════════════════════════════════════════ */

    /**
     * Verifies a Firebase ID token from the Authorization header and
     * returns the corresponding userId (Firebase UID).
     * Returns null if the token is missing, malformed, or expired.
     */
    public String getUserIdFromToken(String authHeader) {
        FirebaseToken decoded = firebaseTokenVerifier.verify(authHeader);
        return decoded != null ? decoded.getUid() : null;
    }

    /* ═══════════════════════════════════════════════════════════
     *  ROLE-BASED QUERY HELPERS
     * ═══════════════════════════════════════════════════════════ */

    /**
     * Returns all users with the {@link Role#ADMIN} role.
     *
     * @return list of admin users
     */
    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    /**
     * Returns all users with the {@link Role#CLIENT} role.
     *
     * @return list of client users
     */
    public List<User> getAllClients() {
        return userRepository.findByRole(Role.CLIENT);
    }

    /**
     * Returns every registered user regardless of role.
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Looks up a single user by their database ID.
     *
     * @param id the user's ID
     * @return the {@link User} entity
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    /**
     * Determines whether a given user ID belongs to an admin.
     *
     * @param userId the user's database ID
     * @return {@code true} if the user is an admin
     */
    public boolean isAdmin(String userId) {
        User user = getUserById(userId);
        return user.isAdmin();
    }

    /**
     * Determines whether a given user ID belongs to a client.
     *
     * @param userId the user's database ID
     * @return {@code true} if the user is a client
     */
    public boolean isClient(String userId) {
        User user = getUserById(userId);
        return user.isClient();
    }

    /* ═══════════════════════════════════════════════════════════
     *  PRIVATE HELPERS
     * ═══════════════════════════════════════════════════════════ */

    /**
     * Converts a role string from the frontend to the {@link Role} enum.
     * Defaults to {@link Role#CLIENT} for any unrecognised value.
     *
     * @param roleStr the role string (e.g. "admin", "client")
     * @return the corresponding {@link Role} enum constant
     */
    private Role resolveRole(String roleStr) {
        if (roleStr != null && roleStr.equalsIgnoreCase("admin")) {
            return Role.ADMIN;
        }
        return Role.CLIENT;
    }

    /**
     * Builds an {@link AuthResponse} from a persisted {@link User},
     * generating a UUID-based token for the session.
     *
     * @param user the authenticated / newly registered user
     * @return a populated {@link AuthResponse}
     */
    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                null,
                user.getRoleEnum().name().toLowerCase(),
                user.getEmail(),
                user.getId());
    }
}
