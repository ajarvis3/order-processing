package com.example.palpay.controllers;

import com.example.palpay.dto.OrderConfirmResponse;
import com.example.palpay.dto.PaymentCaptureResponse;
import com.example.palpay.dto.PaymentVoidResponse;
import com.example.palpay.service.OrderService;
import com.example.palpay.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/palpay/payments")
public class PalpayPaymentsController {

    @Autowired
    private PaymentsService paymentService;

    @PostMapping("/{authId}/confirm")
    public PaymentCaptureResponse capturePayment(@PathVariable String authId) {
        return paymentService.capture(authId);

    }

    @PostMapping("/{authId}/void")
    public PaymentVoidResponse voidPayment(@PathVariable String authId) {
        return paymentService.voidPayment(authId);

    }
}