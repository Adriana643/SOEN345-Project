package com.soen345.Ticket.Reservation.Application.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Registration Logic – creating users based on role")
    class RegistrationTests {

        @Test
        @DisplayName("Should register a new CLIENT user successfully")
        void registerClientUser() {
            RegisterRequest request = new RegisterRequest("Alice", "alice@test.com", "pass123", "client");

            AuthResponse response = userService.registerUser(request);

            assertNotNull(response.getToken(), "Token should be generated");
            assertEquals("client", response.getRole(), "Role should be 'client'");
            assertEquals("alice@test.com", response.getEmail());
            assertNotNull(response.getId(), "User ID should be assigned");
        }

        @Test
        @DisplayName("Should register a new ADMIN user successfully")
        void registerAdminUser() {
            RegisterRequest request = new RegisterRequest("Bob", "bob@test.com", "admin123", "admin");

            AuthResponse response = userService.registerUser(request);

            assertNotNull(response.getToken());
            assertEquals("admin", response.getRole(), "Role should be 'admin'");
            assertEquals("bob@test.com", response.getEmail());
        }

        @Test
        @DisplayName("Should persist user with correct role in database")
        void registerPersistsCorrectRole() {
            userService.registerUser(new RegisterRequest("Charlie", "charlie@test.com", "pass", "admin"));

            User savedUser = userRepository.findByEmail("charlie@test.com").orElseThrow();
            assertEquals(Role.ADMIN, savedUser.getRole());
            assertEquals("Charlie", savedUser.getName());
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
            userService.registerUser(new RegisterRequest("Eve", "eve@test.com", "pass1", "client"));

            RegisterRequest duplicate = new RegisterRequest("Eve2", "eve@test.com", "pass2", "admin");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.registerUser(duplicate)
            );
            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Should allow registering both a client and admin with different emails")
        void registerBothRoles() {
            userService.registerUser(new RegisterRequest("Client1", "client1@test.com", "p1", "client"));
            userService.registerUser(new RegisterRequest("Admin1", "admin1@test.com", "p2", "admin"));

            List<User> clients = userRepository.findByRole(Role.CLIENT);
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            assertEquals(1, clients.size());
            assertEquals(1, admins.size());
            assertEquals("Client1", clients.get(0).getName());
            assertEquals("Admin1", admins.get(0).getName());
        }
    }

 

    @Nested
    @DisplayName("User Logic – distinguishing client from admin requests")
    class LoginTests {

        @BeforeEach
        void seedUsers() {
            userService.registerUser(new RegisterRequest("Client", "client@test.com", "clientpass", "client"));
            userService.registerUser(new RegisterRequest("Admin", "admin@test.com", "adminpass", "admin"));
        }

        @Test
        @DisplayName("Should login client with correct credentials and role")
        void loginClientSuccess() {
            LoginRequest request = new LoginRequest("client@test.com", "clientpass", "client");

            AuthResponse response = userService.loginUser(request);

            assertNotNull(response.getToken());
            assertEquals("client", response.getRole());
            assertEquals("client@test.com", response.getEmail());
        }

        @Test
        @DisplayName("Should login admin with correct credentials and role")
        void loginAdminSuccess() {
            LoginRequest request = new LoginRequest("admin@test.com", "adminpass", "admin");

            AuthResponse response = userService.loginUser(request);

            assertNotNull(response.getToken());
            assertEquals("admin", response.getRole());
            assertEquals("admin@test.com", response.getEmail());
        }

        @Test
        @DisplayName("Should reject login with wrong password")
        void loginWrongPassword() {
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

        @BeforeEach
        void seedUsers() {
            userService.registerUser(new RegisterRequest("C1", "c1@test.com", "p", "client"));
            userService.registerUser(new RegisterRequest("C2", "c2@test.com", "p", "client"));
            userService.registerUser(new RegisterRequest("A1", "a1@test.com", "p", "admin"));
        }

        @Test
        @DisplayName("getAllClients() returns only client users")
        void getAllClients() {
            List<User> clients = userService.getAllClients();
            assertEquals(2, clients.size());
            assertTrue(clients.stream().allMatch(User::isClient));
        }

        @Test
        @DisplayName("getAllAdmins() returns only admin users")
        void getAllAdmins() {
            List<User> admins = userService.getAllAdmins();
            assertEquals(1, admins.size());
            assertTrue(admins.stream().allMatch(User::isAdmin));
        }

        @Test
        @DisplayName("getAllUsers() returns all users regardless of role")
        void getAllUsers() {
            List<User> all = userService.getAllUsers();
            assertEquals(3, all.size());
        }

        @Test
        @DisplayName("isAdmin() returns true for admin users")
        void isAdminCheck() {
            User admin = userRepository.findByEmail("a1@test.com").orElseThrow();
            assertTrue(userService.isAdmin(admin.getId()));
            assertFalse(userService.isClient(admin.getId()));
        }

        @Test
        @DisplayName("isClient() returns true for client users")
        void isClientCheck() {
            User client = userRepository.findByEmail("c1@test.com").orElseThrow();
            assertTrue(userService.isClient(client.getId()));
            assertFalse(userService.isAdmin(client.getId()));
        }

        @Test
        @DisplayName("getUserById() throws for non-existent ID")
        void getUserByIdNotFound() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.getUserById(99999L)
            );
        }
    }
}
