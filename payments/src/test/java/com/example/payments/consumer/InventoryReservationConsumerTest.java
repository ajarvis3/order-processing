package com.example.payments.consumer;

import com.example.inventory.dto.ReservationEvent;
import com.example.payments.service.PaymentsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationConsumerTest {

    @Mock
    private PaymentsService paymentsService;

    @InjectMocks
    private InventoryReservationConsumer consumer;

    @Test
    void consumeDelegatesToService() {
        ReservationEvent event = new ReservationEvent(42L, "SKU-10", 5, true);

        consumer.consume(event);

        verify(paymentsService).handleInventoryReservation(42L, "SKU-10", 5, true);
    }

    @Test
    void consumeSkipsServiceWhenEventIsNull() {
        consumer.consume(null);

        verifyNoInteractions(paymentsService);
    }
}
