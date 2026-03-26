package com.example.orders.dto;

public record OrderRequest(
    String sku,
    String orderNumber,
    Double totalAmount,
    Integer orderQuantity,
    Long palpayOrder
) { }