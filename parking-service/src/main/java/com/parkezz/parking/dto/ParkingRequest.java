package com.parkezz.parking.dto;

import com.parkezz.parking.entity.ParkingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRequest {

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Price per hour is required")
    @Positive(message = "Price per hour must be greater than zero")
    private BigDecimal pricePerHour;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Status is required")
    private ParkingStatus status;
}
