package com.parkezz.booking.repository;

import com.parkezz.booking.entity.Booking;
import com.parkezz.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    /**
     * Finds any existing (non-cancelled) booking for the same parking space
     * that overlaps the requested time window using the simple rule:
     * requestedStart < existingEnd AND requestedEnd > existingStart
     */
    @Query("SELECT b FROM Booking b WHERE b.parkingId = :parkingId " +
           "AND b.status <> com.parkezz.booking.entity.BookingStatus.CANCELLED " +
           "AND :requestedStart < b.endTime AND :requestedEnd > b.startTime")
    List<Booking> findOverlappingBookings(@Param("parkingId") Long parkingId,
                                           @Param("requestedStart") LocalDateTime requestedStart,
                                           @Param("requestedEnd") LocalDateTime requestedEnd);
}
