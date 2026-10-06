package com.parkezz.parking.service;

import com.parkezz.parking.dto.ParkingRequest;
import com.parkezz.parking.dto.ParkingResponse;
import com.parkezz.parking.entity.Parking;
import com.parkezz.parking.entity.ParkingStatus;
import com.parkezz.parking.exception.ParkingNotFoundException;
import com.parkezz.parking.exception.UnauthorizedActionException;
import com.parkezz.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingServiceImpl implements ParkingService {

    private final ParkingRepository parkingRepository;

    @Override
    public ParkingResponse create(ParkingRequest request, Long ownerId) {
        Parking parking = Parking.builder()
                .ownerId(ownerId)
                .location(request.getLocation())
                .address(request.getAddress())
                .pricePerHour(request.getPricePerHour())
                .vehicleType(request.getVehicleType())
                .status(request.getStatus())
                .build();

        Parking saved = parkingRepository.save(parking);
        log.info("Parking created successfully with id={} by ownerId={}", saved.getParkingId(), ownerId);
        return toResponse(saved);
    }

    @Override
    public List<ParkingResponse> getAll() {
        return parkingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ParkingResponse getById(Long id) {
        Parking parking = findOrThrow(id);
        return toResponse(parking);
    }

    @Override
    public List<ParkingResponse> search(String location, String vehicleType, ParkingStatus status) {
        log.info("Parking search requested: location={}, vehicleType={}, status={}", location, vehicleType, status);
        return parkingRepository.search(location, vehicleType, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ParkingResponse update(Long id, ParkingRequest request, Long ownerId) {
        Parking parking = findOrThrow(id);
        verifyOwnership(parking, ownerId);

        parking.setLocation(request.getLocation());
        parking.setAddress(request.getAddress());
        parking.setPricePerHour(request.getPricePerHour());
        parking.setVehicleType(request.getVehicleType());
        parking.setStatus(request.getStatus());

        Parking saved = parkingRepository.save(parking);
        log.info("Parking updated successfully: id={}", id);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id, Long ownerId) {
        Parking parking = findOrThrow(id);
        verifyOwnership(parking, ownerId);
        parkingRepository.delete(parking);
        log.info("Parking deleted successfully: id={}", id);
    }

    private Parking findOrThrow(Long id) {
        return parkingRepository.findById(id).orElseThrow(() -> {
            log.warn("Parking not found: id={}", id);
            return new ParkingNotFoundException("Parking space not found with id: " + id);
        });
    }

    private void verifyOwnership(Parking parking, Long ownerId) {
        if (!parking.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedActionException("You do not own this parking space");
        }
    }

    private ParkingResponse toResponse(Parking parking) {
        return ParkingResponse.builder()
                .parkingId(parking.getParkingId())
                .ownerId(parking.getOwnerId())
                .location(parking.getLocation())
                .address(parking.getAddress())
                .pricePerHour(parking.getPricePerHour())
                .vehicleType(parking.getVehicleType())
                .status(parking.getStatus())
                .build();
    }
}
