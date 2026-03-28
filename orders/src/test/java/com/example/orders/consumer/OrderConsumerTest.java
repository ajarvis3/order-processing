package com.example.orders.consumer;

import com.example.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderConsumer orderConsumer;

    // ── consume ───────────────────────────────────────────────────────────────────

    @Test
    void consume_delegatesToOrderService() {
        orderConsumer.consume(10L);

        verify(orderService).updateOrderStatusToShipped(10L);
    }

    @Test
    void consume_callsServiceExactlyOnce() {
        orderConsumer.consume(42L);

        verify(orderService, times(1)).updateOrderStatusToShipped(42L);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void consume_whenServiceThrows_propagatesException() {
        doThrow(new RuntimeException("update failed"))
                .when(orderService).updateOrderStatusToShipped(99L);

        assertThrows(RuntimeException.class, () -> orderConsumer.consume(99L));
    }
}

