package com.parkezz.parking.controller;

import com.parkezz.parking.dto.ParkingRequest;
import com.parkezz.parking.dto.ParkingResponse;
import com.parkezz.parking.entity.ParkingStatus;
import com.parkezz.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@Tag(name = "Parking", description = "Create, search and manage parking listings")
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping
    @Operation(summary = "Create a new parking listing (OWNER only)")
    public ResponseEntity<ParkingResponse> create(@Valid @RequestBody ParkingRequest request,
                                                   Authentication authentication) {
        Long ownerId = Long.parseLong(authentication.getName());
        ParkingResponse response = parkingService.create(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all parking spaces")
    public List<ParkingResponse> getAll() {
        return parkingService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a parking space by ID")
    public ParkingResponse getById(@PathVariable Long id) {
        return parkingService.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search parking by location, vehicle type and/or status")
    public List<ParkingResponse> search(@RequestParam(required = false) String location,
                                         @RequestParam(required = false) String vehicleType,
                                         @RequestParam(required = false) ParkingStatus status) {
        return parkingService.search(location, vehicleType, status);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update your own parking listing (OWNER only)")
    public ParkingResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ParkingRequest request,
                                   Authentication authentication) {
        Long ownerId = Long.parseLong(authentication.getName());
        return parkingService.update(id, request, ownerId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your own parking listing (OWNER only)")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long ownerId = Long.parseLong(authentication.getName());
        parkingService.delete(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
