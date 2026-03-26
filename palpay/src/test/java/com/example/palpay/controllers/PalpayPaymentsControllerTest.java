package com.example.palpay.controllers;

import com.example.palpay.dto.PaymentCaptureResponse;
import com.example.palpay.dto.PaymentVoidResponse;
import com.example.palpay.service.PaymentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PalpayPaymentsController.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@ActiveProfiles("test")
class PalpayPaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentsService paymentsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void capturePayment_returnsCaptureResponse() throws Exception {
        PaymentCaptureResponse resp = new PaymentCaptureResponse(10L, "COMPLETED");
        when(paymentsService.capture(eq("AUTH-1"))).thenReturn(resp);

        mockMvc.perform(post("/palpay/payments/AUTH-1/confirm")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(10)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void voidPayment_returnsVoidResponse() throws Exception {
        PaymentVoidResponse resp = new PaymentVoidResponse(11L, "VOIDED");
        when(paymentsService.voidPayment(eq("AUTH-2"))).thenReturn(resp);

        mockMvc.perform(post("/palpay/payments/AUTH-2/void")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(11)))
                .andExpect(jsonPath("$.status", is("VOIDED")));
    }
}

