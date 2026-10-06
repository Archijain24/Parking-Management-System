package com.parkezz.parking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.parkezz.parking.entity.ParkingStatus;

class ParkingDtoTest {

    @Test
    void errorResponse_gettersSettersAndConstructors_work() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(
                timestamp,
                404,
                "Parking not found",
                "/api/parking/1"
        );

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Parking not found");
        assertThat(response.getPath()).isEqualTo("/api/parking/1");

        response.setTimestamp(timestamp.plusMinutes(1));
        response.setStatus(500);
        response.setMessage("Server error");
        response.setPath("/error");

        assertThat(response.getTimestamp()).isEqualTo(timestamp.plusMinutes(1));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Server error");
        assertThat(response.getPath()).isEqualTo("/error");
    }

    @Test
    void errorResponse_builderAndEqualsHashCodeToString_work() {
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response1 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .message("Bad request")
                .path("/api/parking")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .message("Bad request")
                .path("/api/parking")
                .build();

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("Bad request");
    }

    @Test
    void parkingRequest_gettersSettersAndConstructor_work() {
        ParkingRequest request = new ParkingRequest(
                "Pune",
                "Main Road",
                BigDecimal.valueOf(50),
                "CAR",
                ParkingStatus.AVAILABLE
        );

        assertThat(request.getLocation()).isEqualTo("Pune");
        assertThat(request.getAddress()).isEqualTo("Main Road");
        assertThat(request.getPricePerHour()).isEqualByComparingTo("50");
        assertThat(request.getVehicleType()).isEqualTo("CAR");
        assertThat(request.getStatus()).isEqualTo(ParkingStatus.AVAILABLE);

        request.setLocation("Mumbai");
        request.setAddress("MG Road");
        request.setPricePerHour(BigDecimal.valueOf(75));
        request.setVehicleType("BIKE");
        request.setStatus(ParkingStatus.UNAVAILABLE);

        assertThat(request.getLocation()).isEqualTo("Mumbai");
        assertThat(request.getAddress()).isEqualTo("MG Road");
        assertThat(request.getPricePerHour()).isEqualByComparingTo("75");
        assertThat(request.getVehicleType()).isEqualTo("BIKE");
        assertThat(request.getStatus()).isEqualTo(ParkingStatus.UNAVAILABLE);
    }

    @Test
    void parkingRequest_equalsHashCodeToString_work() {
        ParkingRequest request1 = new ParkingRequest(
                "Pune",
                "Main Road",
                BigDecimal.valueOf(50),
                "CAR",
                ParkingStatus.AVAILABLE
        );

        ParkingRequest request2 = new ParkingRequest(
                "Pune",
                "Main Road",
                BigDecimal.valueOf(50),
                "CAR",
                ParkingStatus.AVAILABLE
        );

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.toString()).contains("Pune");
    }

    @Test
    void parkingResponse_gettersSettersAndConstructor_work() {
        ParkingResponse response = new ParkingResponse(
                1L,
                10L,
                "Pune",
                "Main Road",
                BigDecimal.valueOf(100),
                "CAR",
                ParkingStatus.AVAILABLE
        );

        assertThat(response.getParkingId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(10L);
        assertThat(response.getLocation()).isEqualTo("Pune");
        assertThat(response.getAddress()).isEqualTo("Main Road");
        assertThat(response.getPricePerHour()).isEqualByComparingTo("100");
        assertThat(response.getVehicleType()).isEqualTo("CAR");
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.AVAILABLE);

        response.setParkingId(2L);
        response.setOwnerId(20L);
        response.setLocation("Mumbai");
        response.setAddress("MG Road");
        response.setPricePerHour(BigDecimal.valueOf(150));
        response.setVehicleType("BIKE");
        response.setStatus(ParkingStatus.UNAVAILABLE);

        assertThat(response.getParkingId()).isEqualTo(2L);
        assertThat(response.getOwnerId()).isEqualTo(20L);
        assertThat(response.getLocation()).isEqualTo("Mumbai");
        assertThat(response.getAddress()).isEqualTo("MG Road");
        assertThat(response.getPricePerHour()).isEqualByComparingTo("150");
        assertThat(response.getVehicleType()).isEqualTo("BIKE");
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.UNAVAILABLE);
    }

    @Test
    void parkingResponse_builderEqualsHashCodeAndToString_work() {
        ParkingResponse response1 = ParkingResponse.builder()
                .parkingId(1L)
                .ownerId(10L)
                .location("Pune")
                .address("Main Road")
                .pricePerHour(BigDecimal.valueOf(100))
                .vehicleType("CAR")
                .status(ParkingStatus.AVAILABLE)
                .build();

        ParkingResponse response2 = ParkingResponse.builder()
                .parkingId(1L)
                .ownerId(10L)
                .location("Pune")
                .address("Main Road")
                .pricePerHour(BigDecimal.valueOf(100))
                .vehicleType("CAR")
                .status(ParkingStatus.AVAILABLE)
                .build();

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("Pune");
    }

    @Test
    void noArgsConstructors_work() {
        assertThat(new ErrorResponse()).isNotNull();
        assertThat(new ParkingRequest()).isNotNull();
        assertThat(new ParkingResponse()).isNotNull();
    }
}