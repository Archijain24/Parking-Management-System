package com.parkezz.booking.service;

import com.parkezz.booking.client.ParkingServiceClient;
import com.parkezz.booking.dto.BookingRequest;
import com.parkezz.booking.dto.BookingResponse;
import com.parkezz.booking.dto.ParkingResponse;
import com.parkezz.booking.entity.Booking;
import com.parkezz.booking.entity.BookingStatus;
import com.parkezz.booking.exception.*;
import com.parkezz.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ParkingServiceClient parkingServiceClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequest request;
    private ParkingResponse availableParking;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(2);
        request = new BookingRequest(1L, start, end);

        availableParking = new ParkingResponse(1L, 10L, "Downtown", "123 Main St",
                BigDecimal.valueOf(50), "CAR", "AVAILABLE");
    }

    @Test
    void create_validRequest_createsBookingWithPendingStatus() {
        when(parkingServiceClient.getParkingById(eq(1L), anyString())).thenReturn(availableParking);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1L);
            return b;
        });

        BookingResponse response = bookingService.create(request, 100L, "Bearer test-token");

        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100)); // 2 hrs * 50
    }

    @Test
    void create_partialHour_roundsUpToNextWholeHour() {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = start.plusMinutes(61);
        BookingRequest partialHour = new BookingRequest(1L, start, end);

        when(parkingServiceClient.getParkingById(eq(1L), anyString())).thenReturn(availableParking);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.create(partialHour, 100L, "Bearer test-token");

        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void create_endTimeBeforeStartTime_throwsInvalidBookingRequestException() {
        BookingRequest invalid = new BookingRequest(1L, request.getEndTime(), request.getStartTime());

        assertThatThrownBy(() -> bookingService.create(invalid, 100L, "Bearer test-token"))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void create_parkingUnavailable_throwsParkingUnavailableException() {
        ParkingResponse unavailable = new ParkingResponse(1L, 10L, "Downtown", "123 Main St",
                BigDecimal.valueOf(50), "CAR", "UNAVAILABLE");
        when(parkingServiceClient.getParkingById(eq(1L), anyString())).thenReturn(unavailable);

        assertThatThrownBy(() -> bookingService.create(request, 100L, "Bearer test-token"))
                .isInstanceOf(ParkingUnavailableException.class);
    }

    @Test
    void create_parkingNotFound_throwsParkingUnavailableException() {
        when(parkingServiceClient.getParkingById(eq(1L), anyString()))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> bookingService.create(request, 100L, "Bearer test-token"))
                .isInstanceOf(ParkingUnavailableException.class);
    }

    @Test
    void create_overlappingBooking_throwsBookingConflictException() {
        when(parkingServiceClient.getParkingById(eq(1L), anyString())).thenReturn(availableParking);
        Booking existing = Booking.builder().bookingId(5L).parkingId(1L).userId(200L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> bookingService.create(request, 100L, "Bearer test-token"))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void cancel_ownBooking_setsStatusCancelled() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancel(1L, 100L);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancel_notOwner_throwsUnauthorizedActionException() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.cancel(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void getById_bookingNotFound_throwsBookingNotFoundException() {
        when(bookingRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(42L, 100L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void create_nonOverlappingBooking_succeeds() {
        when(parkingServiceClient.getParkingById(eq(1L), anyString())).thenReturn(availableParking);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.create(request, 100L, "Bearer test-token");

        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void getById_ownBooking_returnsBookingResponse() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        BookingResponse response = bookingService.getById(1L, 100L);

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(100L);
    }

    @Test
    void getById_notOwner_throwsUnauthorizedActionException() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.getById(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void getMyBookings_returnsOnlyCurrentUsersBookings() {
        Booking booking1 = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findByUserId(100L)).thenReturn(List.of(booking1));

        List<BookingResponse> responses = bookingService.getMyBookings(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void getMyBookings_noBookings_returnsEmptyList() {
        when(bookingRepository.findByUserId(200L)).thenReturn(Collections.emptyList());

        List<BookingResponse> responses = bookingService.getMyBookings(200L);

        assertThat(responses).isEmpty();
    }

    @Test
    void cancel_bookingNotFound_throwsBookingNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(99L, 100L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void confirm_pendingBooking_setsStatusConfirmed() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.confirm(1L, 100L);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirm_notOwner_throwsUnauthorizedActionException() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.confirm(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void confirm_alreadyConfirmedBooking_throwsInvalidBookingRequestException() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.confirm(1L, 100L))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessageContaining("Only a pending booking can be confirmed");
    }

    @Test
    void confirm_cancelledBooking_throwsInvalidBookingRequestException() {
        Booking existing = Booking.builder().bookingId(1L).userId(100L).parkingId(1L)
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .status(BookingStatus.CANCELLED).totalAmount(BigDecimal.TEN).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.confirm(1L, 100L))
                .isInstanceOf(InvalidBookingRequestException.class);
    }

    @Test
    void confirm_bookingNotFound_throwsBookingNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirm(99L, 100L))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
