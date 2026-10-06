package com.parkezz.booking.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class BookingEntityTest {

    @Test
    void booking_gettersSettersAndConstructor_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        Booking booking = new Booking(
                1L,
                10L,
                20L,
                start,
                end,
                BigDecimal.valueOf(250),
                BookingStatus.PENDING
        );

        assertThat(booking.getBookingId()).isEqualTo(1L);
        assertThat(booking.getUserId()).isEqualTo(10L);
        assertThat(booking.getParkingId()).isEqualTo(20L);
        assertThat(booking.getStartTime()).isEqualTo(start);
        assertThat(booking.getEndTime()).isEqualTo(end);
        assertThat(booking.getTotalAmount()).isEqualByComparingTo("250");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        booking.setBookingId(2L);
        booking.setUserId(11L);
        booking.setParkingId(21L);
        booking.setStartTime(start.plusDays(1));
        booking.setEndTime(end.plusDays(1));
        booking.setTotalAmount(BigDecimal.valueOf(300));
        booking.setStatus(BookingStatus.CONFIRMED);

        assertThat(booking.getBookingId()).isEqualTo(2L);
        assertThat(booking.getUserId()).isEqualTo(11L);
        assertThat(booking.getParkingId()).isEqualTo(21L);
        assertThat(booking.getTotalAmount()).isEqualByComparingTo("300");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void booking_builderEqualsHashCodeAndToString_work() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 10, 12, 0);

        Booking booking1 = Booking.builder()
                .bookingId(1L)
                .userId(10L)
                .parkingId(20L)
                .startTime(start)
                .endTime(end)
                .totalAmount(BigDecimal.valueOf(250))
                .status(BookingStatus.PENDING)
                .build();

        Booking booking2 = Booking.builder()
                .bookingId(1L)
                .userId(10L)
                .parkingId(20L)
                .startTime(start)
                .endTime(end)
                .totalAmount(BigDecimal.valueOf(250))
                .status(BookingStatus.PENDING)
                .build();

        assertThat(booking1).isEqualTo(booking2);
        assertThat(booking1.hashCode()).isEqualTo(booking2.hashCode());
        assertThat(booking1.toString()).contains("bookingId=1");
    }

    @Test
    void booking_noArgsConstructor_works() {
        Booking booking = new Booking();

        assertThat(booking).isNotNull();
        assertThat(booking.getBookingId()).isNull();
        assertThat(booking.getUserId()).isNull();
        assertThat(booking.getParkingId()).isNull();
        assertThat(booking.getStartTime()).isNull();
        assertThat(booking.getEndTime()).isNull();
        assertThat(booking.getTotalAmount()).isNull();
        assertThat(booking.getStatus()).isNull();
    }

    @Test
    void bookingStatus_containsExpectedValues() {
        assertThat(BookingStatus.values())
                .containsExactly(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED,
                        BookingStatus.CANCELLED,
                        BookingStatus.COMPLETED
                );

        assertThat(BookingStatus.valueOf("PENDING"))
                .isEqualTo(BookingStatus.PENDING);

        assertThat(BookingStatus.valueOf("CONFIRMED"))
                .isEqualTo(BookingStatus.CONFIRMED);

        assertThat(BookingStatus.valueOf("CANCELLED"))
                .isEqualTo(BookingStatus.CANCELLED);

        assertThat(BookingStatus.valueOf("COMPLETED"))
                .isEqualTo(BookingStatus.COMPLETED);
    }
}