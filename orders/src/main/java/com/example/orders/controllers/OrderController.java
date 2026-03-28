package com.example.orders.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.dto.PalpayAuthIdResponse;
import com.example.orders.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/{id}/palpay-order")
    public PalpayAuthIdResponse getPalpayOrder(@PathVariable Long id) {
        String authId = orderService.getPalpayAuthId(id);
        return new PalpayAuthIdResponse(authId);
    }
}