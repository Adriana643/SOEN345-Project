package com.soen345.Ticket.Reservation.Application.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    @DisplayName("Constructor should set all fields correctly")
    void constructorSetsFields() {
        User user = new User("John", "john@test.com", "secret", Role.CLIENT);

        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals(Role.CLIENT, user.getRole());
    }

    @Test
    @DisplayName("isAdmin() returns true only for ADMIN role")
    void isAdminCheck() {
        User admin = new User("Admin", "a@test.com", "p", Role.ADMIN);
        User client = new User("Client", "c@test.com", "p", Role.CLIENT);

        assertTrue(admin.isAdmin());
        assertFalse(client.isAdmin());
    }

    @Test
    @DisplayName("isClient() returns true only for CLIENT role")
    void isClientCheck() {
        User admin = new User("Admin", "a@test.com", "p", Role.ADMIN);
        User client = new User("Client", "c@test.com", "p", Role.CLIENT);

        assertTrue(client.isClient());
        assertFalse(admin.isClient());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void settersWork() {
        User user = new User();
        user.setId(1L);
        user.setName("Updated");
        user.setEmail("updated@test.com");
        user.setPassword("newpass");
        user.setRole(Role.ADMIN);

        assertEquals(1L, user.getId());
        assertEquals("Updated", user.getName());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.isAdmin());
    }

    @Test
    @DisplayName("Role enum has exactly CLIENT and ADMIN values")
    void roleEnumValues() {
        Role[] roles = Role.values();
        assertEquals(2, roles.length);
        assertEquals(Role.CLIENT, Role.valueOf("CLIENT"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("Default constructor creates user with null fields")
    void defaultConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }
}
