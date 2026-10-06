package com.parkezz.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors the fields Booking Service needs from Parking Service's response.
 * Kept local (no shared library) so each service stays independently
 * deployable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingResponse {
    private Long parkingId;
    private Long ownerId;
    private String location;
    private String address;
    private BigDecimal pricePerHour;
    private String vehicleType;
    private String status;
}
