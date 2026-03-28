package com.example.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.example.orders.controllers.OrderController;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.service.OrderService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

import tools.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.UUID;

@WebMvcTest(OrderController.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ActiveProfiles("test")
class OrdersApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void testCreateOrder() throws Exception {
        Long randomId = generateRandomId();
        String randomUUID = generateRandomUUID();
        String randomSku = generateRandomSku();
        Integer orderAmount = generateRandomOrderAmount();
        Long palpayOrder = generateRandomId();


        OrderRequest request = new OrderRequest(randomSku, randomUUID, 99.99, orderAmount, palpayOrder);
        OrderResponse response = new OrderResponse(randomSku, randomId, randomUUID, "PENDING", 99.99, orderAmount, null);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNumber", is(randomUUID)))
            .andExpect(jsonPath("$.totalAmount", is(99.99)))
            .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void testGetOrder() throws Exception {
        Long randomId = generateRandomId();
        String randomUUID = generateRandomUUID();
        String randomSku = generateRandomSku();
        Integer orderAmount = generateRandomOrderAmount();

        OrderResponse response = new OrderResponse(randomSku, randomId, randomUUID, "PENDING", 149.99, orderAmount, null);

        when(orderService.getOrder(anyLong())).thenReturn(response);

        mockMvc.perform(get("/orders/" + randomId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(randomId))  // fixed
            .andExpect(jsonPath("$.orderNumber", is(randomUUID)))
            .andExpect(jsonPath("$.totalAmount", is(149.99)))
            .andExpect(jsonPath("$.status", is("PENDING")));
    }

    private Long generateRandomId() {
        return Math.abs(new Random().nextLong() % 1_000_000L);
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public String generateRandomSku() {
        int length = 6;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("SKU-");

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    public Integer generateRandomOrderAmount() {
        Random random = new Random();
        return random.nextInt() % 12;
    }
}