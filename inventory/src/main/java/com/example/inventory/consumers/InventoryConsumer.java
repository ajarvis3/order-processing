package com.example.inventory.consumers;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    @Autowired
    private InventoryService inventoryService;

    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void consume(InventoryRequest message) {
        inventoryService.reserve(message.orderId(), message.sku(), message.quantity()); // delegate to service
    }
}