package com.example.orders.controllers;

import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_returnsOrderResponse() throws Exception {
        OrderRequest request = new OrderRequest("SKU-ABC", "ORD-001", 49.99, 2, 10L);
        OrderResponse response = new OrderResponse("SKU-ABC", 1L, "ORD-001", "AUTHORIZED", 49.99, 2, "AUTH-10");

        given(orderService.createOrder(any(OrderRequest.class))).willReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sku").value("SKU-ABC"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));
    }

    @Test
    void getOrder_returnsOrderResponse() throws Exception {
        Long orderId = 5L;
        OrderResponse response = new OrderResponse("SKU-XYZ", 5L, "ORD-005", "PENDING", 99.99, 1, null);

        given(orderService.getOrder(eq(orderId))).willReturn(response);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sku").value("SKU-XYZ"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.orderNumber").value("ORD-005"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_whenNotFound_returnsOkWithNullBody() throws Exception {
        Long orderId = 999L;

        given(orderService.getOrder(eq(orderId))).willReturn(null);

        mockMvc.perform(get("/orders/{id}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getPalpayOrder_returns200WithPalpayId() throws Exception {
        Long orderId = 42L;
        String authId = "AUTH-123";

        given(orderService.getPalpayAuthId(eq(orderId))).willReturn(authId);

        mockMvc.perform(get("/orders/{id}/palpay-order", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.authId").value(authId));
    }

    @Test
    void getPalpayOrder_whenMissing_returns404() throws Exception {
        Long orderId = 99L;

        given(orderService.getPalpayAuthId(eq(orderId)))
                .willThrow(new ResourceNotFoundException("Order not found: " + orderId));

        mockMvc.perform(get("/orders/{id}/palpay-order", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

