package com.example.payments.service;

import com.example.payments.dto.PaymentCaptureResponse;
import com.example.payments.dto.PaymentVoidResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.payments.dto.PalpayAuthDto;
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
	
	@Autowired
	KafkaTemplate<String, Long> kafkaTemplate;
	
	private static String KAFKA_TOPIC = "ready-for-shipping";

	/**
	 * Handle inventory reservation events coming from the inventory service.
	 * This is a simple placeholder implementation: log and decide next steps.
	 *
	 * @param sku the SKU reserved
	 * @param quantityAvailable remaining quantity available
	 * @param reserved whether the reservation succeeded
	 */
	@Transactional
	public void handleInventoryReservation(Long orderId, String sku, int quantityAvailable, boolean reserved) {
		// Fetch the authId once and decide flow based on its presence. If the Orders
		// service indicates no authId we will attempt to void the payment by order id.
		log.info("Inventory {} for sku='{}' qty={}. Handling payment for orderId={}",
				reserved ? "reserved" : "NOT reserved", sku, quantityAvailable, orderId);
		Optional<String> optAuth = null;
		try {
			optAuth = getPalpayAuthIdForOrder(orderId);
			if (optAuth.isPresent()) {
				String authId = optAuth.get();
				if (reserved) {
					log.info("AuthId {} found for orderId={}; capturing payment", authId, orderId);
					callPalpayCapture(authId);
					publishPaymentsResultForShipping(orderId);
				} else {
					log.info("AuthId {} found for orderId={}; voiding payment by auth", authId, orderId);
					callPalpayVoid(authId);
				}
			}
		} catch (RuntimeException e) {
			// If we can't fetch the auth id, attempt a best-effort void-by-order and rethrow
			log.error("Error fetching authId for orderId={}; attempting void-by-order as fallback", orderId, e);
			try {
				if (optAuth != null && optAuth.isPresent()) callPalpayVoid(optAuth.get());
			} catch (Exception voidEx) {
				log.error("Failed to void payment by order id={} after auth lookup failure", orderId, voidEx);
			}
			throw e;
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

	private void publishPaymentsResultForShipping(Long orderId) {
		try {
			kafkaTemplate.send(KAFKA_TOPIC, orderId);
		} catch (Exception e) {
			throw new RuntimeException("Failed to publish payments completion result to Kafka", e);
		}
	}
}

