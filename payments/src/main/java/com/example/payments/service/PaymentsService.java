package com.example.payments.service;

import com.example.payments.dto.PaymentCaptureResponse;
import com.example.payments.dto.PaymentVoidResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentsService {
	private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

	// Base URL for the Palpay service; defaults to localhost:8080 if not configured
	@Value("${palpay.base-url:http://localhost:8080}")
	private String palpayBaseUrl;

	// Simple RestTemplate for outgoing HTTP calls. For production consider injecting a
	// RestTemplateBuilder-configured RestTemplate or use WebClient for reactive calls.
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Handle inventory reservation events coming from the inventory service.
	 * This is a simple placeholder implementation: log and decide next steps.
	 *
	 * @param sku the SKU reserved
	 * @param quantityAvailable remaining quantity available
	 * @param reserved whether the reservation succeeded
	 */
	public void handleInventoryReservation(String sku, int quantityAvailable, boolean reserved) {
		if (reserved) {
			log.info("Inventory reserved for sku='{}' qty={} — proceed to capture payment or mark order ready", sku, quantityAvailable);
			// TODO: call payment capture flow or notify order system
		} else {
			log.warn("Inventory NOT reserved for sku='{}' qty={} — do not capture payment; consider compensating actions", sku, quantityAvailable);
			// TODO: implement compensation (release holds, notify order failure)
		}
	}

	/**
	 * Call Palpay to capture a payment that was previously authorized.
	 * Maps the Palpay response into the local {@link PaymentCaptureResponse} record.
	 *
	 * @param authId authorization id
	 * @return response from Palpay (orderId and status)
	 * @throws RestClientException if the HTTP request fails
	 */
	public PaymentCaptureResponse callPalpayCapture(String authId) {
		String url = palpayBaseUrl + "/palpay/payments/{authId}/confirm";
		try {
			PaymentCaptureResponse resp = restTemplate.postForObject(url, null, PaymentCaptureResponse.class, authId);
			if (resp == null) {
				throw new RestClientException("Empty response from Palpay capture for authId=" + authId);
			}
			return resp;
		} catch (RestClientException e) {
			log.error("Failed to call Palpay capture for authId={}", authId, e);
			throw e;
		}
	}

	/**
	 * Call Palpay to void (cancel) a previously authorized payment.
	 * Maps the Palpay response into the local {@link PaymentVoidResponse} record.
	 *
	 * @param authId authorization id
	 * @return response from Palpay (orderId and status)
	 * @throws RestClientException if the HTTP request fails
	 */
	public PaymentVoidResponse callPalpayVoid(String authId) {
		String url = palpayBaseUrl + "/palpay/payments/{authId}/void";
		try {
			PaymentVoidResponse resp = restTemplate.postForObject(url, null, PaymentVoidResponse.class, authId);
			if (resp == null) {
				throw new RestClientException("Empty response from Palpay void for authId=" + authId);
			}
			return resp;
		} catch (RestClientException e) {
			log.error("Failed to call Palpay void for authId={}", authId, e);
			throw e;
		}
	}
}


