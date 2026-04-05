package com.soen345.Ticket.Reservation.Application.service;

import com.soen345.Ticket.Reservation.Application.config.FirebaseTokenVerifier;

import com.soen345.Ticket.Reservation.Application.dto.AuthResponse;
import com.soen345.Ticket.Reservation.Application.dto.LoginRequest;
import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;

import com.soen345.Ticket.Reservation.Application.model.Role;
import com.soen345.Ticket.Reservation.Application.model.User;
import com.soen345.Ticket.Reservation.Application.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @InjectMocks
    private UserService userService;



    // Helper to build a User with a given role
    private User buildUser(String id, String name, String email, String password, Role role) {
        return new User(id, name, email, password, role);
    }

    /** Registration tests **/
    @Nested
    @DisplayName("Registration Logic – creating users based on role")
    class RegistrationTests {

        @BeforeEach
        void setUp() {
            // Default: email does not exist yet
            lenient().when(userRepository.existsByEmail(any())).thenReturn(false);
            lenient().when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        }

        @Test
        @DisplayName("Should register a new CLIENT user successfully")
        void registerClientUser() {
            RegisterRequest request = new RegisterRequest("Alice", "alice@test.com", "pass123", "client");

            AuthResponse response = userService.registerUser(request);

            assertNull(response.getToken(), "Token should be null");
            assertEquals("client", response.getRole(), "Role should be 'client'");
            assertEquals("alice@test.com", response.getEmail());
            assertNotNull(response.getId(), "User ID should be assigned");
        }

        @Test
        @DisplayName("Should register a new ADMIN user successfully")
        void registerAdminUser() {
            RegisterRequest request = new RegisterRequest("Bob", "bob@test.com", "admin123", "admin");

            AuthResponse response = userService.registerUser(request);

            assertNull(response.getToken());
            assertEquals("admin", response.getRole(), "Role should be a admin");
            assertEquals("bob@test.com", response.getEmail());


        }

        @Test
        @DisplayName("Should persist user with correct role in database")
        void registerPersistsCorrectRole() {
            userService.registerUser(
                    new RegisterRequest("Charlie", "charlie@test.com", "pass", "admin"));

            // Verify save was called with a User that has ADMIN role, correct name and email
            verify(userRepository).save(argThat(user ->
                    "ADMIN".equals(user.getRole()) &&
                            "Charlie".equals(user.getName()) &&
                            "charlie@test.com".equals(user.getEmail())
            ));
        }

        @Test
        @DisplayName("Should default to CLIENT role for unknown role strings")
        void registerDefaultsToClient() {
            RegisterRequest request = new RegisterRequest("Dana", "dana@test.com", "pass", "unknown");

            AuthResponse response = userService.registerUser(request);

            assertEquals("client", response.getRole());
        }

        @Test
        @DisplayName("Should reject duplicate email registration")
        void registerDuplicateEmailThrows() {
            when(userRepository.existsByEmail("eve@test.com")).thenReturn(true);
            RegisterRequest duplicate = new RegisterRequest("Eve2", "eve@test.com", "pass2", "admin");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.registerUser(duplicate)
            );
            assertTrue(ex.getMessage().contains("already exists"));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow registering both a client and admin with different emails")
        void registerBothRoles() {
            userService.registerUser(new RegisterRequest("Client1", "client1@test.com", "p1", "client"));
            userService.registerUser(new RegisterRequest("Admin1", "admin1@test.com", "p2", "admin"));

            verify(userRepository).save(argThat(u ->
                    "CLIENT".equals(u.getRole()) && "Client1".equals(u.getName())));
            verify(userRepository).save(argThat(u ->
                    "ADMIN".equals(u.getRole())  && "Admin1".equals(u.getName())));
        }
    }

    /* Login tests */
    @Nested
    @DisplayName("User Logic – distinguishing client from admin requests")
    class LoginTests {

        private User clientUser;
        private User adminUser;

        @BeforeEach
        void setUp() {
            // Build users directly — no registration needed, avoids mock setup complexity
            clientUser = buildUser("client-id", "Client", "client@test.com", "clientpass", Role.CLIENT);
            adminUser  = buildUser("admin-id",  "Admin",  "admin@test.com",  "adminpass",  Role.ADMIN);
        }


        @Test
        @DisplayName("Should login client with correct credentials and role")
        void loginClientSuccess() {
            when(userRepository.findByEmail("client@test.com"))
                    .thenReturn(Optional.of(clientUser));

            LoginRequest request = new LoginRequest("client@test.com", "clientpass", "client");

            AuthResponse response = userService.loginUser(request);

            assertNull(response.getToken());
            assertEquals("client", response.getRole());
            assertEquals("client@test.com", response.getEmail());
        }

        @Test
        @DisplayName("Should login admin with correct credentials and role")
        void loginAdminSuccess() {
            when(userRepository.findByEmail("admin@test.com"))
                    .thenReturn(Optional.of(adminUser));
            LoginRequest request = new LoginRequest("admin@test.com", "adminpass", "admin");

            AuthResponse response = userService.loginUser(request);

            assertNull(response.getToken());
            assertEquals("admin", response.getRole());
            assertEquals("admin@test.com", response.getEmail());
        }

        @Test
        @DisplayName("Should reject login with wrong password")
        void loginWrongPassword() {
            when(userRepository.findByEmail("client@test.com"))
                    .thenReturn(Optional.of(clientUser));
            LoginRequest request = new LoginRequest("client@test.com", "wrongpass", "client");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.loginUser(request)
            );
            assertTrue(ex.getMessage().contains("Invalid email or password"));
        }

        @Test
        @DisplayName("Should reject login with non-existent email")
        void loginNonExistentEmail() {
            when(userRepository.findByEmail("nobody@test.com"))
                    .thenReturn(Optional.empty());
            LoginRequest request = new LoginRequest("nobody@test.com", "pass", "client");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.loginUser(request)
            );
            assertTrue(ex.getMessage().contains("Invalid email or password"));
        }

        @Test
        @DisplayName("Should reject client trying to login as admin (role mismatch)")
        void loginRoleMismatchClientAsAdmin() {
            when(userRepository.findByEmail("client@test.com"))
                    .thenReturn(Optional.of(clientUser));
            LoginRequest request = new LoginRequest("client@test.com", "clientpass", "admin");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.loginUser(request)
            );
            assertTrue(ex.getMessage().contains("registered as a client"));
        }

        @Test
        @DisplayName("Should reject admin trying to login as client (role mismatch)")
        void loginRoleMismatchAdminAsClient() {
            when(userRepository.findByEmail("admin@test.com"))
                    .thenReturn(Optional.of(adminUser));
            LoginRequest request = new LoginRequest("admin@test.com", "adminpass", "client");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.loginUser(request)
            );
            assertTrue(ex.getMessage().contains("registered as a admin"));
        }
    }


    @Nested
    @DisplayName("Role-based queries")
    class RoleQueryTests {

        private User client1;
        private User client2;
        private User admin1;

        @BeforeEach
        void setUp() {
            // Build users directly — no registration needed
            client1 = buildUser("c1-id", "C1", "c1@test.com", "p", Role.CLIENT);
            client2 = buildUser("c2-id", "C2", "c2@test.com", "p", Role.CLIENT);
            admin1  = buildUser("a1-id", "A1", "a1@test.com", "p", Role.ADMIN);
        }

        @Test
        @DisplayName("getAllClients() returns only client users")
        void getAllClients() {
            when(userRepository.findByRole(Role.CLIENT))
                    .thenReturn(List.of(client1, client2));

            List<User> clients = userService.getAllClients();
            assertEquals(2, clients.size());
            assertTrue(clients.stream().allMatch(User::isClient));
        }

        @Test
        @DisplayName("getAllAdmins() returns only admin users")
        void getAllAdmins() {
            when(userRepository.findByRole(Role.ADMIN))
                    .thenReturn(List.of(admin1));

            List<User> admins = userService.getAllAdmins();
            assertEquals(1, admins.size());
            assertTrue(admins.stream().allMatch(User::isAdmin));
        }

        @Test
        @DisplayName("getAllUsers() returns all users regardless of role")
        void getAllUsers() {
            when(userRepository.findAll())
                    .thenReturn(List.of(client1, client2, admin1));
            List<User> all = userService.getAllUsers();
            assertEquals(3, all.size());
        }

        @Test
        @DisplayName("isAdmin() returns true for admin users")
        void isAdminCheck() {
            when(userRepository.findById("a1-id"))
                    .thenReturn(Optional.of(admin1));

            assertTrue(userService.isAdmin(admin1.getId()));
            assertFalse(userService.isClient(admin1.getId()));
        }

        @Test
        @DisplayName("isClient() returns true for client users")
        void isClientCheck() {
            when(userRepository.findById("c1-id"))
                    .thenReturn(Optional.of(client1));;
            assertTrue(userService.isClient(client1.getId()));
            assertFalse(userService.isAdmin(client1.getId()));
        }

        @Test
        @DisplayName("getUserById() throws for non-existent ID")
        void getUserByIdNotFound() {
            when(userRepository.findById("bad-id"))
                    .thenReturn(Optional.empty());
            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.getUserById("bad-id")
            );
        }
    }
}
