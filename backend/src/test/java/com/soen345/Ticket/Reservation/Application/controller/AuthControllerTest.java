package com.soen345.Ticket.Reservation.Application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soen345.Ticket.Reservation.Application.dto.LoginRequest;
import com.soen345.Ticket.Reservation.Application.dto.RegisterRequest;
import com.soen345.Ticket.Reservation.Application.repository.UserRepository;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



/**
 * Integration tests for {@link AuthController}.
 */

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Timestamp suffix ensures no collisions across parallel CI runs
    private static final long   SUFFIX            = System.currentTimeMillis();
    private static final String CLIENT_UID         = "test-client-uid-" + SUFFIX;
    private static final String CLIENT_EMAIL       = "testclient+" + SUFFIX + "@test.com";
    private static final String CLIENT_PASSWORD    = "clientpass123";

    private static final String ADMIN_UID          = "test-admin-uid-" + SUFFIX;
    private static final String ADMIN_EMAIL        = "testadmin+" + SUFFIX + "@test.com";
    private static final String ADMIN_PASSWORD     = "adminpass123";

    //Seed
    @BeforeAll
    static void seedTestUsers(@Autowired MockMvc mvc) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        RegisterRequest clientReq = new RegisterRequest(
                "Test Client", CLIENT_EMAIL, CLIENT_PASSWORD, "client", CLIENT_UID);
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(clientReq)))
           .andExpect(status().isCreated());

        RegisterRequest adminReq = new RegisterRequest(
                "Test Admin", ADMIN_EMAIL, ADMIN_PASSWORD, "admin", ADMIN_UID);
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminReq)))
        .andExpect(status().isCreated());
    }

    @AfterAll
    static void cleanupTestUsers(@Autowired UserRepository repo) {
        try { repo.deleteById(CLIENT_UID); } catch (Exception ignored) {}
        try { repo.deleteById(ADMIN_UID);  } catch (Exception ignored) {}
    }


    // ─────────────────────────────────────────────────────────────
    //  POST /api/auth/register
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {
        // Track UIDs created in register tests so we can clean them up
        private String createdUid;

        @AfterEach
        void cleanup() {
            if (createdUid != null) {
                try { userRepository.deleteById(createdUid); } catch (Exception ignored) {}
            }
        }

        @Test
        @DisplayName("Should register a client and return 201")
        void register_success() throws Exception {
            long ts = System.currentTimeMillis();
            createdUid = "reg-client-uid-" + ts;

            RegisterRequest req = new RegisterRequest(
                    "New Client", "newclient+" + ts + "@test.com",
                    "pass123", "client", createdUid);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("client"))
                    .andExpect(jsonPath("$.email").value("newclient+" + ts + "@test.com"))
                    .andExpect(jsonPath("$.id").value(createdUid));
        }

        @Test
        @DisplayName("Should register an admin and return 201")
        void registerAdmin() throws Exception {
            long ts = System.currentTimeMillis();
            createdUid = "reg-admin-uid-" + ts;

            RegisterRequest req = new RegisterRequest(
                    "New Admin", "newadmin+" + ts + "@test.com",
                    "admin123", "admin", createdUid);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("admin"))
                    .andExpect(jsonPath("$.id").value(createdUid));
        }
        @Test
        @DisplayName("Should return 400 when registering with a duplicate email")
        void registerDuplicateEmail() throws Exception {
            // CLIENT_EMAIL already exists from @BeforeAll — no new user created
            RegisterRequest req = new RegisterRequest(
                    "Duplicate", CLIENT_EMAIL, "anypass", "client", "duplicate-uid-" + SUFFIX);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("An account with this email already exists."));
        }


        @Test
        @DisplayName("Should return 400 when Firebase UID is missing")
        void registerMissingUid() throws Exception {
            long ts = System.currentTimeMillis();

            RegisterRequest req = new RegisterRequest(
                    "No UID", "nouid+" + ts + "@test.com", "pass123", "client", null);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Firebase UID is required for registration."));
        }
    }



    // ─────────────────────────────────────────────────────────────
    //  POST /api/auth/login
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should login a client with correct credentials")
        void loginClient() throws Exception {
            LoginRequest req = new LoginRequest(CLIENT_EMAIL, CLIENT_PASSWORD, "client");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("client"))
                    .andExpect(jsonPath("$.email").value(CLIENT_EMAIL))
                    .andExpect(jsonPath("$.id").value(CLIENT_UID));
             }


        @Test
        @DisplayName("Should login an admin with correct credentials")
        void loginAdmin() throws Exception {
            LoginRequest req = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD, "admin");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("admin"))
                    .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                    .andExpect(jsonPath("$.id").value(ADMIN_UID));
        }


        @Test
        @DisplayName("Should return 401 for wrong password")
        void loginWrongPassword() throws Exception {
            LoginRequest req = new LoginRequest(CLIENT_EMAIL, "wrongpassword", "client");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password."));
        }

        @Test
        @DisplayName("Should return 401 for non-existent email")
        void loginNonExistentEmail() throws Exception {
            LoginRequest req = new LoginRequest("ghost@test.com", "pass", "client");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password."));
        }




        @Test
        @DisplayName("Should return 401 when client tries to login as admin (role mismatch)")
        void loginRoleMismatch() throws Exception {
            LoginRequest req = new LoginRequest(CLIENT_EMAIL, CLIENT_PASSWORD, "admin");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("This account is registered as a client. Please select the correct role."));
        }

        @Test
        @DisplayName("Should return 401 when admin tries to login as client (role mismatch)")
        void loginAdminAsClient() throws Exception {
            LoginRequest req = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD, "client");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("This account is registered as a admin. Please select the correct role."));
        }
    }
}

