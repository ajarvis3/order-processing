package com.example.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InventoryRequest(
    String sku,
    Integer quantity
) {}

