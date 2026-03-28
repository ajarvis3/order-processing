package com.example.payments.service;

import com.example.payments.dto.PaymentCaptureResponse;
import com.example.payments.dto.PaymentVoidResponse;
import com.example.payments.dto.PalpayAuthDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, Long> kafkaTemplate;

    @InjectMocks
    private PaymentsService paymentsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentsService, "palpayBaseUrl", "http://palpay");
        ReflectionTestUtils.setField(paymentsService, "ordersBaseUrl", "http://orders");
        ReflectionTestUtils.setField(paymentsService, "restTemplate", restTemplate);
    }

    // ── callPalpayCapture ─────────────────────────────────────────────────────────

    @Test
    void callPalpayCaptureReturnsResponseOnSuccess() {
        PaymentCaptureResponse expected = new PaymentCaptureResponse(1L, "CAPTURED");
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentCaptureResponse.class), eq("AUTH-1")))
                .thenReturn(expected);

        PaymentCaptureResponse result = paymentsService.callPalpayCapture("AUTH-1");

        assertEquals(expected, result);
    }

    @Test
    void callPalpayCaptureThrowsWhenResponseIsNull() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentCaptureResponse.class), eq("AUTH-1")))
                .thenReturn(null);

        assertThrows(RestClientException.class, () -> paymentsService.callPalpayCapture("AUTH-1"));
    }

    @Test
    void callPalpayCaptureRethrowsRestClientException() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentCaptureResponse.class), eq("AUTH-1")))
                .thenThrow(new RestClientException("network error"));

        assertThrows(RestClientException.class, () -> paymentsService.callPalpayCapture("AUTH-1"));
    }

    // ── callPalpayVoid ────────────────────────────────────────────────────────────

    @Test
    void callPalpayVoidReturnsResponseOnSuccess() {
        PaymentVoidResponse expected = new PaymentVoidResponse(2L, "VOIDED");
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentVoidResponse.class), eq("AUTH-2")))
                .thenReturn(expected);

        PaymentVoidResponse result = paymentsService.callPalpayVoid("AUTH-2");

        assertEquals(expected, result);
    }

    @Test
    void callPalpayVoidThrowsWhenResponseIsNull() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentVoidResponse.class), eq("AUTH-2")))
                .thenReturn(null);

        assertThrows(RestClientException.class, () -> paymentsService.callPalpayVoid("AUTH-2"));
    }

    @Test
    void callPalpayVoidRethrowsRestClientException() {
        when(restTemplate.postForObject(anyString(), isNull(), eq(PaymentVoidResponse.class), eq("AUTH-2")))
                .thenThrow(new RestClientException("timeout"));

        assertThrows(RestClientException.class, () -> paymentsService.callPalpayVoid("AUTH-2"));
    }

    // ── getPalpayAuthIdForOrder ───────────────────────────────────────────────────

    @Test
    void getPalpayAuthIdForOrderReturnsEmptyWhenDtoIsNull() {
        when(restTemplate.getForObject(anyString(), any(), eq(1L)))
                .thenReturn(null);

        Optional<String> result = paymentsService.getPalpayAuthIdForOrder(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPalpayAuthIdForOrderReturnsEmptyWhenAuthIdIsNull() throws Exception {
        Object dto = buildPalpayAuthDto(null);
        doReturn(dto).when(restTemplate).getForObject(anyString(), any(), eq(2L));

        Optional<String> result = paymentsService.getPalpayAuthIdForOrder(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPalpayAuthIdForOrderReturnsAuthIdWhenPresent() throws Exception {
        Object dto = buildPalpayAuthDto("AUTH-123");
        doReturn(dto).when(restTemplate).getForObject(anyString(), any(), eq(5L));

        Optional<String> result = paymentsService.getPalpayAuthIdForOrder(5L);

        assertTrue(result.isPresent());
        assertEquals("AUTH-123", result.get());
    }

    @Test
    void getPalpayAuthIdForOrderRethrowsRestClientException() {
        when(restTemplate.getForObject(anyString(), any(), eq(9L)))
                .thenThrow(new RestClientException("orders down"));

        assertThrows(RestClientException.class, () -> paymentsService.getPalpayAuthIdForOrder(9L));
    }

    // ── handleInventoryReservation ────────────────────────────────────────────────

    @Test
    void handleInventoryReservationCapturesWhenReservedAndAuthPresent() {
        PaymentsService spy = spy(paymentsService);
        doReturn(Optional.of("AUTH-1")).when(spy).getPalpayAuthIdForOrder(1L);
        doReturn(new PaymentCaptureResponse(1L, "CAPTURED")).when(spy).callPalpayCapture("AUTH-1");

        spy.handleInventoryReservation(1L, "SKU-A", 10, true);

        verify(spy).callPalpayCapture("AUTH-1");
        verify(spy, never()).callPalpayVoid(any());
        verify(kafkaTemplate).send(eq("ready-for-shipping"), eq(1L));
    }

    @Test
    void handleInventoryReservationVoidsByAuthWhenNotReservedAndAuthPresent() {
        PaymentsService spy = spy(paymentsService);
        doReturn(Optional.of("AUTH-1")).when(spy).getPalpayAuthIdForOrder(1L);
        doReturn(new PaymentVoidResponse(1L, "VOIDED")).when(spy).callPalpayVoid("AUTH-1");

        spy.handleInventoryReservation(1L, "SKU-A", 0, false);

        verify(spy).callPalpayVoid("AUTH-1");
        verify(spy, never()).callPalpayCapture(any());
    }

    @Test
    void handleInventoryReservationVoidsByOrderIdWhenNoAuthPresent() {
        PaymentsService spy = spy(paymentsService);
        doReturn(Optional.empty()).when(spy).getPalpayAuthIdForOrder(1L);

        spy.handleInventoryReservation(1L, "SKU-A", 0, true);

        verify(spy, never()).callPalpayCapture(any());
        verify(spy, never()).callPalpayVoid(any());
    }

    @Test
    void handleInventoryReservationRethrowsWhenCaptureThrows() {
        PaymentsService spy = spy(paymentsService);
        doReturn(Optional.of("AUTH-1")).when(spy).getPalpayAuthIdForOrder(1L);
        doThrow(new RestClientException("capture failed")).when(spy).callPalpayCapture("AUTH-1");

        assertThrows(RestClientException.class,
                () -> spy.handleInventoryReservation(1L, "SKU-A", 5, true));
    }

    @Test
    void handleInventoryReservationRethrowsWhenVoidThrows() {
        PaymentsService spy = spy(paymentsService);
        doReturn(Optional.of("AUTH-1")).when(spy).getPalpayAuthIdForOrder(1L);
        doThrow(new RestClientException("void failed")).when(spy).callPalpayVoid("AUTH-1");

        assertThrows(RestClientException.class,
                () -> spy.handleInventoryReservation(1L, "SKU-A", 0, false));
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    /**
     * Reflectively instantiate the private {@code PalpayAuthDto} record inside
     * {@link PaymentsService} so we can stub {@code RestTemplate.getForObject}.
     */
    private static Object buildPalpayAuthDto(String authId) {
        return new PalpayAuthDto(authId);
    }
}
