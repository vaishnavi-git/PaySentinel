package com.payrollaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payrollaudit.dto.AuthRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PayrollAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    @Test
    @Order(1)
    @DisplayName("Admin login should return JWT token")
    void testAdminLogin() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Invalid credentials should return 401")
    void testInvalidLogin() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("admin");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/employees should return employee list when authenticated")
    void testGetEmployees_Authenticated() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/employees should return 401 when unauthenticated")
    void testGetEmployees_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/audit/summary should return audit metrics")
    void testAuditSummary() throws Exception {
        mockMvc.perform(get("/api/audit/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalShifts").exists())
                .andExpect(jsonPath("$.flaggedShifts").exists())
                .andExpect(jsonPath("$.openFlags").exists());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/shifts/flagged should return flagged shifts")
    void testGetFlaggedShifts() throws Exception {
        mockMvc.perform(get("/api/shifts/flagged")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(7)
    @DisplayName("Actuator health endpoint should be publicly accessible")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
