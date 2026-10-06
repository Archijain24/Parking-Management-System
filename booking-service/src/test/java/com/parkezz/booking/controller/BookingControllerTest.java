package com.parkezz.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkezz.booking.config.SecurityConfig;
import com.parkezz.booking.dto.BookingRequest;
import com.parkezz.booking.dto.BookingResponse;
import com.parkezz.booking.entity.BookingStatus;
import com.parkezz.booking.exception.BookingConflictException;
import com.parkezz.booking.exception.BookingNotFoundException;
import com.parkezz.booking.exception.ParkingUnavailableException;
import com.parkezz.booking.security.JwtFilter;
import com.parkezz.booking.security.JwtService;
import com.parkezz.booking.service.BookingService;
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

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtFilter.class, JwtService.class})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static UsernamePasswordAuthenticationToken asUser(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private BookingRequest sampleRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return new BookingRequest(1L, start, start.plusHours(2));
    }

    @Test
    void create_validRequestByCustomer_returns201() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .bookingId(1L).userId(100L).parkingId(1L)
                .startTime(sampleRequest().getStartTime()).endTime(sampleRequest().getEndTime())
                .totalAmount(BigDecimal.valueOf(100)).status(BookingStatus.PENDING).build();
        when(bookingService.create(any(BookingRequest.class), eq(100L), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_ownerRole_isForbidden() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "OWNER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_startTimeInPast_returns400() throws Exception {
        BookingRequest invalid = new BookingRequest(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingParkingId_returns400() throws Exception {
        BookingRequest invalid = new BookingRequest(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_parkingUnavailable_returns400() throws Exception {
        when(bookingService.create(any(BookingRequest.class), eq(100L), anyString()))
                .thenThrow(new ParkingUnavailableException("Parking space is not available for booking"));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_overlappingBooking_returns409() throws Exception {
        when(bookingService.create(any(BookingRequest.class), eq(100L), anyString()))
                .thenThrow(new BookingConflictException("Parking space is already booked for the selected time."));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(asUser(100L, "CUSTOMER")))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void getMyBookings_returnsList() throws Exception {
        when(bookingService.getMyBookings(100L)).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/my").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_ownBooking_returns200() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .bookingId(1L).userId(100L).parkingId(1L).status(BookingStatus.CANCELLED)
                .totalAmount(BigDecimal.TEN).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        when(bookingService.cancel(eq(1L), eq(100L))).thenReturn(response);

        mockMvc.perform(put("/api/bookings/1/cancel").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(bookingService.getById(eq(99L), eq(100L)))
                .thenThrow(new BookingNotFoundException("Booking not found with id: 99"));

        mockMvc.perform(get("/api/bookings/99").with(authentication(asUser(100L, "CUSTOMER"))))
                .andExpect(status().isNotFound());
    }
}
