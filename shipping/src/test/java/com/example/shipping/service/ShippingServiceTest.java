package com.example.shipping.service;

import com.example.shipping.model.Shipping;
import com.example.shipping.repository.ShippingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private KafkaTemplate<String, Long> kafkaTemplate;

    @InjectMocks
    private ShippingService shippingService;

    // ── processShippingRequest ────────────────────────────────────────────────────

    @Test
    void processShippingRequestSavesShippingEntityWithShippedStatus() {
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(inv -> inv.getArgument(0));

        shippingService.processShippingRequest(42L);

        ArgumentCaptor<Shipping> captor = ArgumentCaptor.forClass(Shipping.class);
        verify(shippingRepository).save(captor.capture());
        assertEquals("SHIPPED", captor.getValue().getStatus());
    }

    @Test
    void processShippingRequestPublishesOrderIdToKafkaTopic() {
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(inv -> inv.getArgument(0));

        shippingService.processShippingRequest(7L);

        verify(kafkaTemplate).send(eq("shipping-processed"), eq(7L));
    }

    @Test
    void processShippingRequestSavesBeforePublishing() {
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(inv -> inv.getArgument(0));

        shippingService.processShippingRequest(99L);

        var inOrder = inOrder(shippingRepository, kafkaTemplate);
        inOrder.verify(shippingRepository).save(any(Shipping.class));
        inOrder.verify(kafkaTemplate).send(anyString(), eq(99L));
    }

    @Test
    void processShippingRequestWhenRepositoryThrowsPropagatesException() {
        when(shippingRepository.save(any(Shipping.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> shippingService.processShippingRequest(1L));
        verify(kafkaTemplate, never()).send(anyString(), any(Long.class));
    }

    @Test
    void processShippingRequestWhenKafkaThrowsPropagatesException() {
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(inv -> inv.getArgument(0));
        when(kafkaTemplate.send(anyString(), eq(2L)))
                .thenThrow(new RuntimeException("Kafka error"));

        assertThrows(RuntimeException.class, () -> shippingService.processShippingRequest(2L));
    }
}
