package com.parkezz.parking.dto;

import com.parkezz.parking.entity.ParkingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingResponse {
    private Long parkingId;
    private Long ownerId;
    private String location;
    private String address;
    private BigDecimal pricePerHour;
    private String vehicleType;
    private ParkingStatus status;
}
