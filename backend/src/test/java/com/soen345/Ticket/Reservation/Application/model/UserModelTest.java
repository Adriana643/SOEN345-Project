package com.soen345.Ticket.Reservation.Application.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    @DisplayName("Constructor should set all fields correctly")
    void constructorSetsFields() {
        User user = new User("id-1", "John", "john@test.com", "secret", Role.CLIENT);

        assertEquals("id-1",           user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("secret", user.getPassword());

        // role is stored as String internally
        assertEquals("CLIENT",user.getRole());
        assertEquals(Role.CLIENT, user.getRoleEnum());
    }

    @Test
    @DisplayName("isAdmin() returns true only for ADMIN role")
    void isAdminCheck() {
        User admin = new User("id-1", "Admin",  "a@test.com", "p", Role.ADMIN);
        User client = new User("id-2", "Client", "c@test.com", "p", Role.CLIENT);

        assertTrue(admin.isAdmin());
        assertFalse(client.isAdmin());
    }

    @Test
    @DisplayName("isClient() returns true only for CLIENT role")
    void isClientCheck() {
        User admin = new User("id-1", "Admin",  "a@test.com", "p", Role.ADMIN);
        User client = new User("id-2", "Client", "c@test.com", "p", Role.CLIENT);

        assertTrue(client.isClient());
        assertFalse(admin.isClient());
    }

    @Test
    @DisplayName("Setters update fields correctly")
    void settersWork() {
        User user = new User();
        user.setId("12345");
        user.setName("Updated");
        user.setEmail("updated@test.com");
        user.setPassword("newpass");
        user.setRole("ADMIN");

        assertEquals("12345", user.getId());
        assertEquals("Updated", user.getName());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals("ADMIN", user.getRole());
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
    @DisplayName("getRoleEnum() converts stored String back to Role enum")
    void getRoleEnumCheck() {
        User admin  = new User("id-1", "Admin",  "a@test.com", "p", Role.ADMIN);
        User client = new User("id-2", "Client", "c@test.com", "p", Role.CLIENT);

        assertEquals(Role.ADMIN,  admin.getRoleEnum());
        assertEquals(Role.CLIENT, client.getRoleEnum());
    }

    @Test
    @DisplayName("getRoleEnum() defaults to CLIENT for unrecognised role string")
    void getRoleEnumDefaultsToClient() {
        User user = new User();
        user.setRole("UNKNOWN");

        assertEquals(Role.CLIENT, user.getRoleEnum());
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
