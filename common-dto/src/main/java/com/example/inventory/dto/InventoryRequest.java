package com.example.inventory.dto;

public record InventoryRequest(
    Long orderId,
    String sku,
    Integer quantity
) {}

