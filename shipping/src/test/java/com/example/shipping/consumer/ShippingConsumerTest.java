package com.example.shipping.consumer;

import com.example.shipping.service.ShippingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingConsumerTest {

    @Mock
    private ShippingService shippingService;

    @InjectMocks
    private ShippingConsumer shippingConsumer;

    // ── consume ───────────────────────────────────────────────────────────────────

    @Test
    void consumeDelegatesToShippingService() {
        shippingConsumer.consume(10L);

        verify(shippingService).processShippingRequest(10L);
    }

    @Test
    void consumeCallsServiceExactlyOnce() {
        shippingConsumer.consume(55L);

        verify(shippingService, times(1)).processShippingRequest(55L);
        verifyNoMoreInteractions(shippingService);
    }

    @Test
    void consumeWhenServiceThrows_propagatesException() {
        doThrow(new RuntimeException("service failure"))
                .when(shippingService).processShippingRequest(3L);

        assertThrows(RuntimeException.class, () -> shippingConsumer.consume(3L));
    }
}

