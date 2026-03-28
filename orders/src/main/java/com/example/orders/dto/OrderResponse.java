package com.example.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderResponse(
    String sku,
    Long id,
    String orderNumber,
    String status,
    Double totalAmount,
    Integer orderQuantity,
    String authId
) {}
