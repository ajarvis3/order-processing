package com.example.orders.controllers;

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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

