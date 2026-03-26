package com.example.inventory.service;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.exceptions.InventoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, ReservationEvent> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String INVENTORY_RESULT_TOPIC = "inventory-reservation-result";

    public void reserve(String sku, int qty)  {
        try {
            InventoryItem item = orderRepository.findBySku(sku)
                    .orElseThrow(() -> new InventoryNotFoundException(sku));
            if (item.getQuantityAvailable() >= qty) {
                item.setQuantityAvailable(item.getQuantityAvailable() - qty);
                orderRepository.save(item);
                publishReservationResult(new ReservationEvent(sku, item.getQuantityAvailable(), true));
            } else {
                publishReservationResult(new ReservationEvent(sku, item.getQuantityAvailable(), false));
            }
        } catch (InventoryNotFoundException e) {
            publishReservationResult(new ReservationEvent(sku, 0, false));
            return;
        }

    }

    private void publishReservationResult(ReservationEvent event) {
        try {
            kafkaTemplate.send(INVENTORY_RESULT_TOPIC, event.sku, event);
        } catch (Exception e) {
            // Log error but don't fail the reservation - it's already been processed
            throw new RuntimeException("Failed to publish inventory reservation result to Kafka", e);
        }
    }

    public static class ReservationEvent {
        public String sku;
        public int quantityAvailable;
        public boolean reserved;

        public ReservationEvent(String sku, int quantityAvailable, boolean reserved) {
            this.sku = sku;
            this.quantityAvailable = quantityAvailable;
            this.reserved = reserved;
        }
    }
}
