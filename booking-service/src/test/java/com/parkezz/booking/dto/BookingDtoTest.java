package com.parkezz.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.parkezz.booking.entity.BookingStatus;

class BookingDtoTest {

    @Test
    void bookingRequest_gettersSettersAndConstructor_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        BookingRequest request = new BookingRequest(
                1L,
                start,
                end
        );

        assertThat(request.getParkingId()).isEqualTo(1L);
        assertThat(request.getStartTime()).isEqualTo(start);
        assertThat(request.getEndTime()).isEqualTo(end);

        request.setParkingId(2L);
        request.setStartTime(start.plusDays(1));
        request.setEndTime(end.plusDays(1));

        assertThat(request.getParkingId()).isEqualTo(2L);
        assertThat(request.getStartTime()).isEqualTo(start.plusDays(1));
        assertThat(request.getEndTime()).isEqualTo(end.plusDays(1));
    }

    @Test
    void bookingRequest_equalsHashCodeAndToString_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        BookingRequest request1 =
                new BookingRequest(1L, start, end);

        BookingRequest request2 =
                new BookingRequest(1L, start, end);

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.toString()).contains("parkingId=1");
    }

    @Test
    void bookingResponse_gettersSettersAndConstructor_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        BookingResponse response = new BookingResponse(
                1L,
                10L,
                20L,
                start,
                end,
                BigDecimal.valueOf(250),
                BookingStatus.PENDING
        );

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getParkingId()).isEqualTo(20L);
        assertThat(response.getStartTime()).isEqualTo(start);
        assertThat(response.getEndTime()).isEqualTo(end);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("250");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);

        response.setBookingId(2L);
        response.setUserId(11L);
        response.setParkingId(21L);
        response.setStartTime(start.plusDays(1));
        response.setEndTime(end.plusDays(1));
        response.setTotalAmount(BigDecimal.valueOf(300));
        response.setStatus(BookingStatus.CONFIRMED);

        assertThat(response.getBookingId()).isEqualTo(2L);
        assertThat(response.getUserId()).isEqualTo(11L);
        assertThat(response.getParkingId()).isEqualTo(21L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("300");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bookingResponse_builderEqualsHashCodeAndToString_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        BookingResponse response1 = BookingResponse.builder()
                .bookingId(1L)
                .userId(10L)
                .parkingId(20L)
                .startTime(start)
                .endTime(end)
                .totalAmount(BigDecimal.valueOf(250))
                .status(BookingStatus.PENDING)
                .build();

        BookingResponse response2 = BookingResponse.builder()
                .bookingId(1L)
                .userId(10L)
                .parkingId(20L)
                .startTime(start)
                .endTime(end)
                .totalAmount(BigDecimal.valueOf(250))
                .status(BookingStatus.PENDING)
                .build();

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("bookingId=1");
    }

    @Test
    void errorResponse_gettersSettersAndConstructor_work() {
        LocalDateTime timestamp = LocalDateTime.of(2030, 1, 10, 10, 0);

        ErrorResponse response = new ErrorResponse(
                timestamp,
                404,
                "Booking not found",
                "/api/bookings/1"
        );

        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Booking not found");
        assertThat(response.getPath()).isEqualTo("/api/bookings/1");

        response.setTimestamp(timestamp.plusMinutes(5));
        response.setStatus(500);
        response.setMessage("Server error");
        response.setPath("/error");

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Server error");
        assertThat(response.getPath()).isEqualTo("/error");
    }

    @Test
    void errorResponse_builderEqualsHashCodeAndToString_work() {
        LocalDateTime timestamp = LocalDateTime.of(2030, 1, 10, 10, 0);

        ErrorResponse response1 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .message("Bad request")
                .path("/api/bookings")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .message("Bad request")
                .path("/api/bookings")
                .build();

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("Bad request");
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
                "AVAILABLE"
        );

        assertThat(response.getParkingId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(10L);
        assertThat(response.getLocation()).isEqualTo("Pune");
        assertThat(response.getAddress()).isEqualTo("Main Road");
        assertThat(response.getPricePerHour()).isEqualByComparingTo("100");
        assertThat(response.getVehicleType()).isEqualTo("CAR");
        assertThat(response.getStatus()).isEqualTo("AVAILABLE");

        response.setParkingId(2L);
        response.setOwnerId(20L);
        response.setLocation("Mumbai");
        response.setAddress("MG Road");
        response.setPricePerHour(BigDecimal.valueOf(150));
        response.setVehicleType("BIKE");
        response.setStatus("UNAVAILABLE");

        assertThat(response.getParkingId()).isEqualTo(2L);
        assertThat(response.getOwnerId()).isEqualTo(20L);
        assertThat(response.getLocation()).isEqualTo("Mumbai");
        assertThat(response.getAddress()).isEqualTo("MG Road");
        assertThat(response.getPricePerHour()).isEqualByComparingTo("150");
        assertThat(response.getVehicleType()).isEqualTo("BIKE");
        assertThat(response.getStatus()).isEqualTo("UNAVAILABLE");
    }

    @Test
    void parkingResponse_equalsHashCodeAndToString_work() {
        ParkingResponse response1 = new ParkingResponse(
                1L,
                10L,
                "Pune",
                "Main Road",
                BigDecimal.valueOf(100),
                "CAR",
                "AVAILABLE"
        );

        ParkingResponse response2 = new ParkingResponse(
                1L,
                10L,
                "Pune",
                "Main Road",
                BigDecimal.valueOf(100),
                "CAR",
                "AVAILABLE"
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.toString()).contains("Pune");
    }

    @Test
    void noArgsConstructors_work() {
        assertThat(new BookingRequest()).isNotNull();
        assertThat(new BookingResponse()).isNotNull();
        assertThat(new ErrorResponse()).isNotNull();
        assertThat(new ParkingResponse()).isNotNull();
    }
}