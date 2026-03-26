package com.example.orders.service;

import com.example.orders.dto.InventoryRequest;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.Random;

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

}

