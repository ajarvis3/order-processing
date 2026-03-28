package com.example.payments.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.example.payments.service.PaymentsService;
import com.example.inventory.dto.ReservationEvent;

@Component
public class InventoryReservationConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationConsumer.class);

    private final PaymentsService paymentsService;

    @Autowired
    public InventoryReservationConsumer(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @KafkaListener(topics = "inventory-reservation-result", groupId = "payments-group")
    public void consume(ReservationEvent event) {
        if (event == null) {
            log.warn("Received null inventory reservation event");
            return;
        }

        log.info("Received inventory reservation event for orderId={} sku={} qty={} reserved={}", event.orderId(), event.sku(), event.quantityAvailable(), event.reserved());

        // Delegate to the payments service for any action (e.g., capture/void decisions)
        paymentsService.handleInventoryReservation(event.orderId(), event.sku(), event.quantityAvailable(), event.reserved());
    }
}

