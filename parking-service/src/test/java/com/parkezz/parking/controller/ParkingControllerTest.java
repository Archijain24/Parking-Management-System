package com.parkezz.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkezz.parking.config.SecurityConfig;
import com.parkezz.parking.dto.ParkingRequest;
import com.parkezz.parking.dto.ParkingResponse;
import com.parkezz.parking.entity.ParkingStatus;
import com.parkezz.parking.exception.ParkingNotFoundException;
import com.parkezz.parking.exception.UnauthorizedActionException;
import com.parkezz.parking.security.JwtFilter;
import com.parkezz.parking.security.JwtService;
import com.parkezz.parking.service.ParkingService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ParkingController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtFilter.class, JwtService.class})
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    private static UsernamePasswordAuthenticationToken asUser(long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private ParkingRequest sampleRequest() {
        return new ParkingRequest("Downtown", "123 Main St", BigDecimal.valueOf(50), "CAR", ParkingStatus.AVAILABLE);
    }

    @Test
    void create_byOwner_returns201() throws Exception {
        ParkingResponse response = ParkingResponse.builder()
                .parkingId(1L).ownerId(10L).location("Downtown").address("123 Main St")
                .pricePerHour(BigDecimal.valueOf(50)).vehicleType("CAR").status(ParkingStatus.AVAILABLE).build();
        when(parkingService.create(any(ParkingRequest.class), eq(10L))).thenReturn(response);

        mockMvc.perform(post("/api/parking")
                        .with(authentication(asUser(10L, "OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Downtown"));
    }

    @Test
    void create_byCustomer_isForbidden() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .with(authentication(asUser(10L, "CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_invalidData_blankLocation_returns400() throws Exception {
        ParkingRequest invalid = new ParkingRequest("", "123 Main St", BigDecimal.valueOf(50), "CAR", ParkingStatus.AVAILABLE);

        mockMvc.perform(post("/api/parking")
                        .with(authentication(asUser(10L, "OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_negativePrice_returns400() throws Exception {
        ParkingRequest invalid = new ParkingRequest("Downtown", "123 Main St", BigDecimal.valueOf(-1), "CAR", ParkingStatus.AVAILABLE);

        mockMvc.perform(post("/api/parking")
                        .with(authentication(asUser(10L, "OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_authenticatedUser_returns200() throws Exception {
        when(parkingService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/parking").with(authentication(asUser(10L, "CUSTOMER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/parking"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(parkingService.getById(99L)).thenThrow(new ParkingNotFoundException("Parking space not found with id: 99"));

        mockMvc.perform(get("/api/parking/99").with(authentication(asUser(10L, "CUSTOMER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_byNonOwner_returns403() throws Exception {
        when(parkingService.update(eq(1L), any(ParkingRequest.class), eq(999L)))
                .thenThrow(new UnauthorizedActionException("You do not own this parking space"));

        mockMvc.perform(put("/api/parking/1")
                        .with(authentication(asUser(999L, "OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_byOwner_returns204() throws Exception {
        mockMvc.perform(delete("/api/parking/1").with(authentication(asUser(10L, "OWNER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_byCustomer_isForbidden() throws Exception {
        mockMvc.perform(delete("/api/parking/1").with(authentication(asUser(10L, "CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void search_withQueryParams_returns200() throws Exception {
        when(parkingService.search("Downtown", "CAR", ParkingStatus.AVAILABLE)).thenReturn(List.of());

        mockMvc.perform(get("/api/parking/search")
                        .param("location", "Downtown")
                        .param("vehicleType", "CAR")
                        .param("status", "AVAILABLE")
                        .with(authentication(asUser(10L, "CUSTOMER"))))
                .andExpect(status().isOk());
    }
}
