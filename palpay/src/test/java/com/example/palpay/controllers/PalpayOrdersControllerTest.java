package com.example.palpay.controllers;

import com.example.palpay.dto.OrderAuthorizeResponse;
import com.example.palpay.dto.OrderConfirmResponse;
import com.example.palpay.dto.OrderCreateRequest;
import com.example.palpay.dto.OrderCreateResponse;
import com.example.palpay.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(PalpayOrdersController.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ActiveProfiles("test")
class PalpayOrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_returnsCreatedResponse() throws Exception {
        OrderCreateResponse response = new OrderCreateResponse(1L, "CREATED");
        when(orderService.create(any(OrderCreateRequest.class))).thenReturn(response);

        OrderCreateRequest request = new OrderCreateRequest(100.0, "card");

        mockMvc.perform(post("/palpay/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    void confirmOrder_returnsConfirmResponse() throws Exception {
        OrderConfirmResponse response = new OrderConfirmResponse(2L, "CONFIRMED");
        when(orderService.confirm(eq(2L))).thenReturn(response);

        mockMvc.perform(post("/palpay/orders/2/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(2)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void authorizeOrder_returnsAuthorizeResponse() throws Exception {
        OrderAuthorizeResponse response = new OrderAuthorizeResponse(3L, "AUTH-3", "AUTHORIZED");
        when(orderService.authorize(eq(3L))).thenReturn(response);

        mockMvc.perform(post("/palpay/orders/3/authorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(3)))
                .andExpect(jsonPath("$.authorizationId", is("AUTH-3")))
                .andExpect(jsonPath("$.status", is("AUTHORIZED")));
    }
}

