package com.example.shipping.service;

import com.example.shipping.model.Shipping;
import com.example.shipping.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    @Autowired
    ShippingRepository shippingRepository;

    @Autowired
    KafkaTemplate<String, Long> kafkaTemplate;

    private static final String KAFKA_TOPIC = "shipping-processed";

    public void processShippingRequest(Long orderId) {
        Shipping shipping = new Shipping(orderId, "SHIPPED");
        shippingRepository.save(shipping);
        kafkaTemplate.send(KAFKA_TOPIC, orderId);
    }
}
