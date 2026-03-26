package com.example.orders.service;

import com.example.orders.dto.InventoryRequest;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, InventoryRequest> kafkaTemplate;
    
    private static final String ORDERS_TOPIC = "orders";
    
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order(request.sku(), request.orderNumber(), "PENDING", request.totalAmount(), request.orderQuantity(), request.palpayOrder());
        Order savedOrder = orderRepository.save(order);
        
        // Produce Kafka message
        InventoryRequest inventoryRequest = new InventoryRequest(order.getSku(), order.getOrderQuantity());
        kafkaTemplate.send(ORDERS_TOPIC, savedOrder.getOrderNumber(), inventoryRequest);
        
        return mapToResponse(savedOrder);
    }
    
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        return order != null ? mapToResponse(order) : null;
    }
    
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(order.getSku(), order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotalAmount(), order.getOrderQuantity());
    }
}