package com.example.shipping.consumer;

import com.example.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShippingConsumer.class);

    private final ShippingService shippingService;

    @Autowired
    public ShippingConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @KafkaListener(topics = "shipping-requests", groupId = "shipping-group")
    public void consume(Long orderId) {
        log.info("Received shipping request: {}", orderId);
        shippingService.processShippingRequest(orderId);
    }
}

