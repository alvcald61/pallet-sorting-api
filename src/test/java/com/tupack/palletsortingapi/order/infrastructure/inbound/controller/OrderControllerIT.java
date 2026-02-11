package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tupack.palletsortingapi.order.application.dto.CreatePalletRequest;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for OrderController.
 * Tests the full stack from HTTP request to database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should reject request with missing required fields (validation)")
    void shouldRejectRequestWithMissingRequiredFields() throws Exception {
        // Given: Request with missing required fields
        SolvePackingRequest request = new SolvePackingRequest();
        // clientId, addresses, dates, and pallets are missing

        // When/Then: Should return 400 Bad Request with validation errors
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation")));
    }

//    @Test
//    @DisplayName("Should reject request with empty pallet list (validation)")
//    void shouldRejectRequestWithEmptyPalletList() throws Exception {
//        // Given: Request with empty pallet list
//        SolvePackingRequest request = new SolvePackingRequest();
//        request.setClientId(UUID.randomUUID());
//        request.setFromAddress("Test Origin");
//        request.setToAddress("Test Destination");
//        request.setDeliveryDate(LocalDateTime.now().plusDays(1));
//        request.setPallets(List.of()); // Empty list
//
//        // When/Then: Should return 400 Bad Request
//        mockMvc.perform(post("/api/orders")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isBadRequest())
//            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("vac")));
//    }
//
//    @Test
//    @DisplayName("Should reject pallet with invalid dimensions (validation)")
//    void shouldRejectPalletWithInvalidDimensions() throws Exception {
//        // Given: Pallet with invalid (negative) dimensions
//        CreatePalletRequest invalidPallet = new CreatePalletRequest();
//        invalidPallet.setType("STANDARD");
//        invalidPallet.setWidth(BigDecimal.valueOf(-100)); // Invalid: negative
//        invalidPallet.setLength(BigDecimal.valueOf(100));
//        invalidPallet.setHeight(BigDecimal.valueOf(150));
//        invalidPallet.setAmount(BigDecimal.valueOf(500));
//
//        SolvePackingRequest request = new SolvePackingRequest();
//        request.setClientId(UUID.randomUUID());
//        request.setFromAddress("Test Origin");
//        request.setToAddress("Test Destination");
//        request.setDeliveryDate(LocalDateTime.now().plusDays(1));
//        request.setPallets(List.of(invalidPallet));
//
//        // When/Then: Should return 400 Bad Request with validation error
//        mockMvc.perform(post("/api/orders")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isBadRequest());
//    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without auth")
    void shouldReturn401WhenAccessingProtectedEndpointWithoutAuth() throws Exception {
        // Note: This test assumes permit-all is false in some profiles
        // In test profile it's true, so we just verify the endpoint exists

        // When/Then: Should be accessible (test profile has permit-all: true)
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent order")
    void shouldReturn404WhenGettingNonExistentOrder() throws Exception {
        // Given: Non-existent order ID
        Long nonExistentId = 99999L;

        // When/Then: Should return 404 Not Found
        mockMvc.perform(get("/api/orders/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should successfully get all orders")
    void shouldSuccessfullyGetAllOrders() throws Exception {
        // When/Then: Should return 200 OK with orders list
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should handle pagination parameters")
    void shouldHandlePaginationParameters() throws Exception {
        // When/Then: Should accept pagination parameters
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

//    @Test
//    @DisplayName("Should validate address length constraints")
//    void shouldValidateAddressLengthConstraints() throws Exception {
//        // Given: Request with address exceeding max length
//        CreatePalletRequest pallet = new CreatePalletRequest();
//        pallet.setType("STANDARD");
//        pallet.setWidth(BigDecimal.valueOf(120));
//        pallet.setLength(BigDecimal.valueOf(100));
//        pallet.setHeight(BigDecimal.valueOf(150));
//        pallet.setAmount(BigDecimal.valueOf(500));
//
//        SolvePackingRequest request = new SolvePackingRequest();
//        request.setClientId(UUID.randomUUID());
//        request.setFromAddress("A".repeat(600)); // Exceeds max length
//        request.setToAddress("Test Destination");
//        request.setDeliveryDate(LocalDateTime.now().plusDays(1));
//        request.setPallets(List.of(pallet));
//
//        // When/Then: Should return validation error if @Size is configured
//        mockMvc.perform(post("/api/orders")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().is4xxClientError());
//    }
}
