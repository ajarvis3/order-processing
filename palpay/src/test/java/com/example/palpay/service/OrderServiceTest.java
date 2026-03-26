package com.example.palpay.service;

import com.example.palpay.dto.OrderAuthorizeResponse;
import com.example.palpay.dto.OrderConfirmResponse;
import com.example.palpay.dto.OrderCreateRequest;
import com.example.palpay.dto.OrderCreateResponse;
import com.example.palpay.exception.DataPersistenceException;
import com.example.palpay.exception.ResourceNotFoundException;
import com.example.palpay.model.Order;
import com.example.palpay.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentsService paymentsService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createShouldSaveOrderAndReturnResponse() {
        OrderCreateRequest req = new OrderCreateRequest(123.45, "card");

        Order created = new Order(123.45, "card", "CREATED");
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        OrderCreateResponse resp = orderService.create(req);

        assertEquals(1L, resp.orderId());
        assertEquals("CREATED", resp.status());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void confirmWhenFoundUpdatesStatus() {
        Order order = new Order(50.0, "card", "AUTHORIZED");
        order.setOrderId(2L);
        when(orderRepository.findByOrderId(eq(2L))).thenReturn(order);

        OrderConfirmResponse resp = orderService.confirm(2L);

        assertEquals(2L, resp.orderId());
        assertEquals("CONFIRMED", resp.status());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmWhenMissingThrowsNotFound() {
        when(orderRepository.findByOrderId(eq(999L))).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> orderService.confirm(999L));
    }

    @Test
    void authorizeCallsPaymentAndUpdatesOrder() {
        Order order = new Order(75.0, "card", "CREATED");
        order.setOrderId(3L);
        when(orderRepository.findByOrderId(eq(3L))).thenReturn(order);

        // paymentsService.create will be called; leave it as void (no exception)

        OrderAuthorizeResponse resp = orderService.authorize(3L);

        assertEquals(3L, resp.orderId());
        assertNotNull(resp.authorizationId());
        assertEquals("AUTHORIZED", resp.status());
        verify(paymentsService).create(eq(3L), anyString());
        verify(orderRepository).save(order);
    }

    @Test
    void authorizeWhenPaymentFailsTranslatesToDataPersistenceException() {
        Order order = new Order(75.0, "card", "CREATED");
        order.setOrderId(4L);
        when(orderRepository.findByOrderId(eq(4L))).thenReturn(order);
        doThrow(new DataPersistenceException(new RuntimeException("DB error"))).when(paymentsService).create(eq(4L), anyString());

        assertThrows(DataPersistenceException.class, () -> orderService.authorize(4L));
    }
}

