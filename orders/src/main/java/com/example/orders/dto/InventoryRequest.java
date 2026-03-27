package com.example.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InventoryRequest(
    Long orderId,
    String sku,
    Integer quantity
) {}

