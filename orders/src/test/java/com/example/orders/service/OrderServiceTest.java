package com.example.orders.service;

import com.example.orders.dto.InventoryRequest;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.orders.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, InventoryRequest> kafkaTemplate;

    @InjectMocks
    private com.example.orders.service.OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "palpayBaseUrl", "http://localhost:9999");
    }

    @Test
    void createOrderSavesAndPublishesInventoryRequest() {
        OrderRequest req = new OrderRequest("SKU-ABC", "ORD-123", 99.99, 3, 1L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            // simulate id assignment
            // order entity uses field 'id'
            // reflectively set or use setter
            o.setId(100L);
            return o;
        });

        OrderResponse resp = orderService.createOrder(req);

        assertNotNull(resp);
        assertEquals("SKU-ABC", resp.sku());
        assertEquals(100L, resp.id());
        assertEquals("ORD-123", resp.orderNumber());
        assertEquals(99.99, resp.totalAmount());
        assertEquals(3, resp.orderQuantity());

        // verify kafka publish
        ArgumentCaptor<InventoryRequest> captor = ArgumentCaptor.forClass(InventoryRequest.class);
        verify(kafkaTemplate).send(eq("orders"), eq("ORD-123"), captor.capture());
        InventoryRequest sent = captor.getValue();
        assertEquals("SKU-ABC", sent.sku());
        assertEquals(3, sent.quantity());
    }

    @Test
    void getOrderWhenFoundReturnsResponse() {

        Order order = new Order("SKU-X", "ORD-1", "PENDING", 49.99, 2, 1L);
        // set id via setter
        order.setId(5L);

        when(orderRepository.findById(eq(5L))).thenReturn(Optional.of(order));

        OrderResponse resp = orderService.getOrder(5L);

        assertNotNull(resp);
        assertEquals("SKU-X", resp.sku());
        assertEquals(5L, resp.id());
        assertEquals("ORD-1", resp.orderNumber());
    }

    @Test
    void getOrderWhenMissingReturnsNull() {
        when(orderRepository.findById(eq(999L))).thenReturn(Optional.empty());
        assertNull(orderService.getOrder(999L));
    }

    @Test
    void getPalpayAuthId_whenFoundWithAuthId_returnsAuthId() {
        Order order = new Order("SKU-X", "ORD-1", "AUTHORIZED", 49.99, 2, 1L);
        order.setId(5L);
        order.setAuthId("AUTH-5");

        when(orderRepository.findById(eq(5L))).thenReturn(Optional.of(order));

        String result = orderService.getPalpayAuthId(5L);

        assertEquals("AUTH-5", result);
    }

    @Test
    void getPalpayAuthId_whenOrderMissing_throwsResourceNotFoundException() {
        when(orderRepository.findById(eq(404L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getPalpayAuthId(404L));
    }

    @Test
    void getPalpayAuthId_whenAuthIdIsNull_throwsResourceNotFoundException() {
        Order order = new Order("SKU-Y", "ORD-2", "PENDING", 10.00, 1, 2L);
        order.setId(7L);
        // authId not set, remains null

        when(orderRepository.findById(eq(7L))).thenReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class, () -> orderService.getPalpayAuthId(7L));
    }

}

