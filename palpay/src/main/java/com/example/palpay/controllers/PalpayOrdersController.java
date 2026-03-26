package com.example.palpay.controllers;

import com.example.palpay.dto.OrderAuthorizeResponse;
import com.example.palpay.dto.OrderConfirmResponse;
import com.example.palpay.dto.OrderCreateRequest;
import com.example.palpay.dto.OrderCreateResponse;
import com.example.palpay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/palpay/orders")
public class PalpayOrdersController {


    @Autowired
    private OrderService orderService;

    @PostMapping
    public OrderCreateResponse createOrder(@RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{orderId}/confirm")
    public OrderConfirmResponse confirmOrder(@PathVariable Long orderId) {
        return orderService.confirm(orderId);
    }

    @PostMapping("/{orderId}/authorize")
    public OrderAuthorizeResponse authorizeOrder(@PathVariable Long orderId) {
        return orderService.authorize(orderId);
    }


}