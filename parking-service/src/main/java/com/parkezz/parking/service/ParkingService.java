package com.parkezz.parking.service;

import com.parkezz.parking.dto.ParkingRequest;
import com.parkezz.parking.dto.ParkingResponse;
import com.parkezz.parking.entity.ParkingStatus;

import java.util.List;

public interface ParkingService {
    ParkingResponse create(ParkingRequest request, Long ownerId);
    List<ParkingResponse> getAll();
    ParkingResponse getById(Long id);
    List<ParkingResponse> search(String location, String vehicleType, ParkingStatus status);
    ParkingResponse update(Long id, ParkingRequest request, Long ownerId);
    void delete(Long id, Long ownerId);
}
