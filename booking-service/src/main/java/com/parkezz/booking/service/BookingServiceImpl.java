package com.parkezz.booking.service;

import com.parkezz.booking.client.ParkingServiceClient;
import com.parkezz.booking.dto.BookingRequest;
import com.parkezz.booking.dto.BookingResponse;
import com.parkezz.booking.dto.ParkingResponse;
import com.parkezz.booking.entity.Booking;
import com.parkezz.booking.entity.BookingStatus;
import com.parkezz.booking.exception.*;
import com.parkezz.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ParkingServiceClient parkingServiceClient;

    @Override
    public BookingResponse create(BookingRequest request, Long userId, String bearerToken) {
        log.info("Booking request received: userId={}, parkingId={}", userId, request.getParkingId());

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidBookingRequestException("End time must be after start time");
        }

        ParkingResponse parking = fetchParking(request.getParkingId(), bearerToken);

        if (!"AVAILABLE".equalsIgnoreCase(parking.getStatus())) {
            log.warn("Booking rejected because parking id={} is unavailable", request.getParkingId());
            throw new ParkingUnavailableException("Parking space is not available for booking");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getParkingId(), request.getStartTime(), request.getEndTime());

        if (!overlapping.isEmpty()) {
            log.warn("Booking rejected because parking id={} is already booked for the requested time", request.getParkingId());
            throw new BookingConflictException("Parking space is already booked for the selected time.");
        }

        BigDecimal totalAmount = calculateAmount(parking.getPricePerHour(), request.getStartTime(), request.getEndTime());

        Booking booking = Booking.builder()
                .userId(userId)
                .parkingId(request.getParkingId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created successfully with id={}, status=PENDING", saved.getBookingId());

        return toResponse(saved);
    }

    @Override
    public BookingResponse getById(Long id, Long requesterId) {
        Booking booking = findOrThrow(id);
        if (!booking.getUserId().equals(requesterId)) {
            throw new UnauthorizedActionException("You cannot view another user's booking");
        }
        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse cancel(Long id, Long userId) {
        Booking booking = findOrThrow(id);
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You cannot cancel another user's booking");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking cancelled: id={}", id);
        return toResponse(saved);
    }

    @Override
    public BookingResponse confirm(Long id, Long userId) {
        Booking booking = findOrThrow(id);
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You cannot confirm another user's booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingRequestException("Only a pending booking can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking confirmed after successful payment: id={}", id);
        return toResponse(saved);
    }

    private ParkingResponse fetchParking(Long parkingId, String bearerToken) {
        try {
            return parkingServiceClient.getParkingById(parkingId, bearerToken);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ParkingUnavailableException("Parking space not found with id: " + parkingId);
        }
    }

    private BigDecimal calculateAmount(BigDecimal pricePerHour, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        long billableHours = (minutes + 59) / 60;
        return pricePerHour.multiply(BigDecimal.valueOf(billableHours));
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .parkingId(booking.getParkingId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .build();
    }
}
