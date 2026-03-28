package com.example.orders.service;

import com.example.inventory.dto.InventoryRequest;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.orders.dto.OrderAuthorizeResponse;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, InventoryRequest> kafkaTemplate;
    
    private static final String ORDERS_TOPIC = "orders";
    
    @Value("${palpay.base-url:http://palpay-service:8080}")
    private String palpayBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();
    
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order(request.sku(), request.orderNumber(), "PENDING", request.totalAmount(), request.orderQuantity(), request.palpayOrder());
        Order savedOrder = orderRepository.save(order);

        // 1) Create Palpay order
        try {
            // 2) Authorize the palpay payment to receive an authId
            OrderAuthorizeResponse authResp = restTemplate.postForObject(palpayBaseUrl + "/palpay/orders/{id}/authorize", null, OrderAuthorizeResponse.class, request.palpayOrder());
            logger.info("Order Authorize Response: {}", authResp);
            if (authResp != null && authResp.authorizationId() != null) {
                savedOrder.setAuthId(authResp.authorizationId());
                savedOrder.setStatus("AUTHORIZED");
                orderRepository.save(savedOrder);
            }
        } catch (RestClientException e) {
            // This should be corrected along with the previous to fail if the payment was not authorized
        }

        // Produce Kafka message (include orderId so inventory can correlate/propagate it)
        InventoryRequest inventoryRequest = new InventoryRequest(savedOrder.getId(), order.getSku(), order.getOrderQuantity());
        kafkaTemplate.send(ORDERS_TOPIC, savedOrder.getOrderNumber(), inventoryRequest);

        return mapToResponse(savedOrder);
    }
    
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        return order != null ? mapToResponse(order) : null;
    }

    public String getPalpayAuthId(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new com.example.orders.exception.ResourceNotFoundException("Order not found: " + id));
        String authId = order.getAuthId();
        if (authId == null) {
            throw new com.example.orders.exception.ResourceNotFoundException("Palpay authId not set for order: " + id);
        }
        return authId;
    }
    
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(order.getSku(), order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotalAmount(), order.getOrderQuantity(), order.getAuthId());
    }

    public void updateOrderStatusToShipped(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus("SHIPPED");
            orderRepository.save(order);
        });
    }
}