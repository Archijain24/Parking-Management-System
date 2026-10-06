package com.parkezz.parking.repository;

import com.parkezz.parking.entity.Parking;
import com.parkezz.parking.entity.ParkingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParkingRepository extends JpaRepository<Parking, Long> {

    List<Parking> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Parking p WHERE " +
           "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:vehicleType IS NULL OR LOWER(p.vehicleType) = LOWER(:vehicleType)) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Parking> search(@Param("location") String location,
                          @Param("vehicleType") String vehicleType,
                          @Param("status") ParkingStatus status);
}
