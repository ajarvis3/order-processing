package com.example.payments.service;

import com.example.payments.dto.PaymentCaptureResponse;
import com.example.payments.dto.PaymentVoidResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class PaymentsService {
	private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

	// Base URL for the Palpay service; defaults to localhost:8080 if not configured
	@Value("${palpay.base-url:http://localhost:8080}")
	private String palpayBaseUrl;

	// Base URL for the Orders service; defaults to localhost:8081 if not configured
	@Value("${orders.base-url:http://localhost:8081}")
	private String ordersBaseUrl;

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
	public void handleInventoryReservation(Long orderId, String sku, int quantityAvailable, boolean reserved) {
		// Fetch the authId once and decide flow based on its presence. If the Orders
		// service indicates no authId we will attempt to void the payment by order id.
		log.info("Inventory {} for sku='{}' qty={}. Handling payment for orderId={}",
				reserved ? "reserved" : "NOT reserved", sku, quantityAvailable, orderId);

		Optional<String> optAuth;
		try {
			optAuth = getPalpayAuthIdForOrder(orderId);
		} catch (RuntimeException e) {
			// If we can't fetch the auth id, attempt a best-effort void-by-order and rethrow
			log.error("Error fetching authId for orderId={}; attempting void-by-order as fallback", orderId, e);
			try {
				callPalpayVoidByOrderId(orderId);
			} catch (Exception voidEx) {
				log.error("Failed to void payment by order id={} after auth lookup failure", orderId, voidEx);
			}
			throw e;
		}

		// If auth is present: capture when reserved, otherwise void by auth.
		if (optAuth.isPresent()) {
			String authId = optAuth.get();
			try {
				if (reserved) {
					log.info("AuthId {} found for orderId={}; capturing payment", authId, orderId);
					callPalpayCapture(authId);
				} else {
					log.info("AuthId {} found for orderId={}; voiding payment by auth", authId, orderId);
					callPalpayVoid(authId);
				}
			} catch (RuntimeException e) {
				log.error("Error performing payment action for orderId={} authId={}", orderId, authId, e);
				throw e;
			}
		} else {
			// No auth id — always void by order id to ensure payment is cancelled.
			log.info("No authId found for orderId={}; voiding payment by order id", orderId);
			try {
				callPalpayVoidByOrderId(orderId);
			} catch (RuntimeException e) {
				log.error("Failed to void payment by orderId={}", orderId, e);
				throw e;
			}
		}
		// TODO: notify order system or further compensation if needed
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

	/**
	 * Attempt to void payment using the order id when an authId is not available.
	 * This calls a Palpay endpoint that voids by order id. If your Palpay API differs,
	 * adjust the URL accordingly.
	 */
	public PaymentVoidResponse callPalpayVoidByOrderId(Long orderId) {
		String url = palpayBaseUrl + "/palpay/payments/order/{orderId}/void";
		try {
			PaymentVoidResponse resp = restTemplate.postForObject(url, null, PaymentVoidResponse.class, orderId);
			if (resp == null) {
				throw new RestClientException("Empty response from Palpay void-by-order for orderId=" + orderId);
			}
			return resp;
		} catch (RestClientException e) {
			log.error("Failed to call Palpay void-by-order for orderId={}", orderId, e);
			throw e;
		}
	}

	/**
	 * Query the Orders service for the palpay authorization id (authId) associated with the given order id.
	 * Calls GET {ordersBaseUrl}/orders/{id}/palpay-order and returns an Optional containing the authId if present.
	 */
	public Optional<String> getPalpayAuthIdForOrder(Long orderId) {
		String url = ordersBaseUrl + "/orders/{id}/palpay-order";
		try {
			PalpayAuthDto dto = restTemplate.getForObject(url, PalpayAuthDto.class, orderId);
			if (dto == null || dto.authId() == null) {
				log.info("Orders service returned no authId for orderId={}", orderId);
				return Optional.empty();
			}
			return Optional.of(dto.authId());
		} catch (RestClientException e) {
			log.error("Failed to fetch palpay authId for orderId={}", orderId, e);
			throw e;
		}
	}

	// Internal DTO to map Orders service response { "authId": "AUTH-123" }
	private record PalpayAuthDto(String authId) {}
}

