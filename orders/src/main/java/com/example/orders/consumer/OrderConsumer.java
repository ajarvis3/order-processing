package com.example.orders.consumer;

import com.example.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderService orderService;

    @Autowired
    public OrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "shipping-processed", groupId = "orders-group")
    public void consume(Long orderId) {
        log.info("Received shipping processed event for orderId={}", orderId);
        orderService.updateOrderStatusToShipped(orderId);
    }
}
