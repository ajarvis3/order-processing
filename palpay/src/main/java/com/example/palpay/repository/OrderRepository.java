package com.example.palpay.repository;

import com.example.palpay.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    public Order findByOrderId(Long orderId);
}
