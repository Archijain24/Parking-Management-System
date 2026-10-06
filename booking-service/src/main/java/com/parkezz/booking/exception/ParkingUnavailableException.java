package com.parkezz.booking.exception;

public class ParkingUnavailableException extends RuntimeException {
    public ParkingUnavailableException(String message) {
        super(message);
    }
}
