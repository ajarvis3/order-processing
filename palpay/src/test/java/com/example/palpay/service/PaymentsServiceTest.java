package com.example.palpay.service;

import com.example.palpay.dto.PaymentCaptureResponse;
import com.example.palpay.dto.PaymentVoidResponse;
import com.example.palpay.exception.DataPersistenceException;
import com.example.palpay.exception.ResourceNotFoundException;
import com.example.palpay.model.Payment;
import com.example.palpay.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentsService paymentsService;

    @Test
    void createShouldSavePayment() {
        Long orderId = 42L;
        String authId = "AUTH-42";

        // call
        paymentsService.create(orderId, authId);

        // verify saved entity
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(authId, saved.getAuthId());
        assertEquals(orderId, saved.getOrderId());
        assertEquals("CREATED", saved.getStatus());
    }

    @Test
    void captureWhenPaymentExistsUpdatesAndReturnsResponse() {
        String authId = "AUTH-1";
        Payment payment = new Payment(authId, 10L, "CREATED");
        when(paymentRepository.findByauthId(eq(authId))).thenReturn(payment);

        PaymentCaptureResponse resp = paymentsService.capture(authId);

        assertNotNull(resp);
        assertEquals(10L, resp.orderId());
        assertEquals("CAPTURED", resp.status());
        // verify save called with updated status
        verify(paymentRepository).save(payment);
    }

    @Test
    void captureWhenNotFoundThrowsResourceNotFound() {
        when(paymentRepository.findByauthId(eq("MISSING"))).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> paymentsService.capture("MISSING"));
    }

    @Test
    void voidPaymentWhenPaymentExistsUpdatesAndReturnsResponse() {
        String authId = "AUTH-2";
        Payment payment = new Payment(authId, 11L, "CREATED");
        when(paymentRepository.findByauthId(eq(authId))).thenReturn(payment);

        PaymentVoidResponse resp = paymentsService.voidPayment(authId);

        assertNotNull(resp);
        assertEquals(11L, resp.orderId());
        assertEquals("VOIDED", resp.status());
        verify(paymentRepository).save(payment);
    }

    @Test
    void voidPaymentWhenRepositoryThrowsTranslatesToDataPersistenceException() {
        String authId = "AUTH-3";
        when(paymentRepository.findByauthId(eq(authId))).thenThrow(new DataPersistenceException(new RuntimeException("DB down")));

        assertThrows(DataPersistenceException.class, () -> paymentsService.voidPayment(authId));
    }
}

