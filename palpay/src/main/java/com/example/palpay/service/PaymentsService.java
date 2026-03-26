package com.example.palpay.service;

import com.example.palpay.dto.PaymentCaptureResponse;
import com.example.palpay.dto.PaymentVoidResponse;
import com.example.palpay.exception.DataPersistenceException;
import com.example.palpay.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import com.example.palpay.model.Payment;
import com.example.palpay.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentsService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public void create(Long orderId, String authId) {
        try {
            Payment payment = new Payment(authId, orderId, "CREATED");
            paymentRepository.save(payment);
        } catch (DataAccessException dae) {
            throw new DataPersistenceException(dae);
        }
    }

    @Transactional
    public PaymentCaptureResponse capture(String authId) {
        try {
            Payment payment = paymentRepository.findByauthId(authId);
            if (payment == null) {
                throw new ResourceNotFoundException("Payment not found: " + authId);
            }
            payment.setStatus("CAPTURED");
            paymentRepository.save(payment);
            return new PaymentCaptureResponse(payment.getOrderId(), payment.getStatus());
        } catch (DataAccessException e) {
            throw new DataPersistenceException(e);
        }
    }

    @Transactional
    public PaymentVoidResponse voidPayment(String authId) {
        try {
            Payment payment = paymentRepository.findByauthId(authId);
            if (payment == null) {
                throw new ResourceNotFoundException("Payment not found: " + authId);
            }
            payment.setStatus("VOIDED");
            paymentRepository.save(payment);
            return new PaymentVoidResponse(payment.getOrderId(), payment.getStatus());
        } catch (DataAccessException dae) {
            throw new DataPersistenceException(dae);
        }
    }
}
