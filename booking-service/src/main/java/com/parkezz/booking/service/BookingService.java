package com.parkezz.booking.service;

import com.parkezz.booking.dto.BookingRequest;
import com.parkezz.booking.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse create(BookingRequest request, Long userId, String bearerToken);
    BookingResponse getById(Long id, Long requesterId);
    List<BookingResponse> getMyBookings(Long userId);
    BookingResponse cancel(Long id, Long userId);
    BookingResponse confirm(Long id, Long userId);
}
