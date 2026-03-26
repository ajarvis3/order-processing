package com.example.palpay.service;

import com.example.palpay.dto.*;
import com.example.palpay.exception.DataPersistenceException;
import com.example.palpay.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import com.example.palpay.model.Order;
import com.example.palpay.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentsService paymentService;

    @Transactional
    public OrderAuthorizeResponse authorize(Long orderId) {
        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                throw new ResourceNotFoundException("Order not found: " + orderId);
            }

            String authId = "AUTH-" + orderId; // Simulate auth ID generation
            order.setAuthId(authId);
            order.setStatus("AUTHORIZED");
            paymentService.create(orderId, authId);
            orderRepository.save(order);
            return new OrderAuthorizeResponse(order.getOrderId(), order.getAuthId(), order.getStatus());
        } catch (DataAccessException dae) {
            throw new DataPersistenceException(dae);
        }
    }

    @Transactional
    public OrderConfirmResponse confirm(Long orderId) {
        try {
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                throw new ResourceNotFoundException("Order not found: " + orderId);
            }
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            return new OrderConfirmResponse(order.getOrderId(), order.getStatus());
        } catch (DataAccessException dae) {
            throw new DataPersistenceException(dae);
        }
    }

    @Transactional
    public OrderCreateResponse create(OrderCreateRequest orderCreateRequest) {
        try {
            Order order = new Order(orderCreateRequest.totalAmount(), orderCreateRequest.paymentMethodType(), "CREATED");
            orderRepository.save(order);
            return new OrderCreateResponse(order.getOrderId(), order.getStatus());
        } catch (DataAccessException dae) {
            throw new DataPersistenceException(dae);
        }
    }
}
