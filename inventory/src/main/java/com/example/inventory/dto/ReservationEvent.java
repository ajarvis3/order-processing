package com.example.inventory.dto;

public record ReservationEvent(
    Long orderId,
    String sku,
    int quantityAvailable,
    boolean reserved
) {}

