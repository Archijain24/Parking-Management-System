package com.parkezz.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkezz.payment.config.SecurityConfig;
import com.parkezz.payment.dto.PaymentRequest;
import com.parkezz.payment.dto.PaymentResponse;
import com.parkezz.payment.entity.PaymentMethod;
import com.parkezz.payment.entity.PaymentStatus;
import com.parkezz.payment.exception.PaymentNotFoundException;
import com.parkezz.payment.exception.UnauthorizedActionException;
import com.parkezz.payment.security.JwtFilter;
import com.parkezz.payment.security.JwtService;
import com.parkezz.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtFilter.class, JwtService.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private static UsernamePasswordAuthenticationToken asUser(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void pay_validRequestByCustomer_returns201WithSuccessStatus() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(1L).bookingId(1L).userId(100L).amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.UPI).paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now()).build();
        when(paymentService.pay(any(PaymentRequest.class), eq(100L), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    void pay_ownerRole_isForbidden() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "OWNER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pay_zeroAmount_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.ZERO, PaymentMethod.UPI);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_negativeAmount_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(-10), PaymentMethod.UPI);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_missingBookingId_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(null, BigDecimal.valueOf(100), PaymentMethod.UPI);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_missingAuthorizationHeader_returns400() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.valueOf(100), PaymentMethod.UPI);

        mockMvc.perform(post("/api/payments")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_ownPayment_returns200() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(1L).bookingId(1L).userId(100L).amount(BigDecimal.valueOf(100))
                .paymentMethod(PaymentMethod.UPI).paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now()).build();
        when(paymentService.getById(eq(1L), eq(100L))).thenReturn(response);

        mockMvc.perform(get("/api/payments/1").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(paymentService.getById(eq(99L), eq(100L)))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: 99"));

        mockMvc.perform(get("/api/payments/99").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_anotherUsersPayment_returns403() throws Exception {
        when(paymentService.getById(eq(1L), eq(100L)))
                .thenThrow(new UnauthorizedActionException("You cannot view another user's payment"));

        mockMvc.perform(get("/api/payments/1").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isForbidden());
    }
}
