package com.example.inventory.service;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.exceptions.InventoryNotFoundException;
import com.example.inventory.dto.ReservationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, ReservationEvent> kafkaTemplate;

    private static final String INVENTORY_RESULT_TOPIC = "inventory-reservation-result";

    @Transactional
    public void reserve(Long orderId, String sku, int qty)  {
        InventoryItem item = orderRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));
        if (item.getQuantityAvailable() >= qty) {
            item.setQuantityAvailable(item.getQuantityAvailable() - qty);
            orderRepository.save(item);
            publishReservationResult(new ReservationEvent(orderId, sku, item.getQuantityAvailable(), true));
        } else {
            publishReservationResult(new ReservationEvent(orderId, sku, item.getQuantityAvailable(), false));
        }

    }

    private void publishReservationResult(ReservationEvent event) {
        try {
            kafkaTemplate.send(INVENTORY_RESULT_TOPIC, event.sku(), event);
        } catch (Exception e) {
            // Log error but don't fail the reservation - it's already been processed
            throw new RuntimeException("Failed to publish inventory reservation result to Kafka", e);
        }
    }
}
