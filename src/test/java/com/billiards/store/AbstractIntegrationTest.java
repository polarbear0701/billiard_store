package com.billiards.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Shared setup: the real application against a throwaway PostgreSQL container
 * (so Flyway migrations and JSONB are exercised). The container is started once per JVM and
 * shared, so the Spring context is cached across test classes.
 */
@SpringBootTest(properties = {
    "SERVER_PORT=0",
    "JWT_SECRET=dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIzNDU2Nzg5MA==",
    "JWT_EXPIRATION_MS=3600000",
    "JWT_REFRESH_EXPIRATION_MS=86400000",
    "BANK_URL=http://bank.test/?",
    "USER_ACCOUNT=admin",
    "USER_PASSWORD=admin-pass",
    "APP_ACCOUNTS=staff1:STAFF:staff-pass-1;staff2:STAFF:staff:pass:2;boss2:ADMIN:boss-pass-2"
})
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Logs in and returns the access token. */
    protected String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return json(result).at("/data/accessToken").asString();
    }

    protected String adminToken() throws Exception {
        return login("admin", "admin-pass");
    }
}
