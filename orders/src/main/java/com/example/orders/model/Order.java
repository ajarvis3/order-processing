package com.example.orders.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sku;
    private String orderNumber;
    private String status;
    private Double totalAmount;
    private Integer orderQuantity;
    private Long palpayOrderId;
    private String authId;
    
    public Order() {}
    
    public Order(String sku, String orderNumber, String status, Double totalAmount, Integer orderQuantity, Long palpayOrderId) {
        this.sku = sku;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderQuantity = orderQuantity;
        this.palpayOrderId = palpayOrderId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(Integer orderQuantity) { this.orderQuantity = orderQuantity; }

    public Long getPalpayOrderId() { return palpayOrderId; }
    public void setPalpayOrderId(Long palpayOrderId) { this.palpayOrderId = palpayOrderId; }

    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }
}