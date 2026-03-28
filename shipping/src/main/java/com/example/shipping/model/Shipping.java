package com.example.shipping.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shipping")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long orderId;

    String status;

    public Shipping() {
    }

    public Shipping(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
