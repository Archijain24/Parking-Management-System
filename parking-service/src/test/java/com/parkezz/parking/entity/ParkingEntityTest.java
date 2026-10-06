package com.parkezz.parking.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ParkingEntityTest {

    @Test
    void parking_gettersSettersAndConstructor_work() {
        Parking parking = new Parking(
                1L,
                10L,
                "Pune",
                "Main Road",
                BigDecimal.valueOf(100),
                "CAR",
                ParkingStatus.AVAILABLE
        );

        assertThat(parking.getParkingId()).isEqualTo(1L);
        assertThat(parking.getOwnerId()).isEqualTo(10L);
        assertThat(parking.getLocation()).isEqualTo("Pune");
        assertThat(parking.getAddress()).isEqualTo("Main Road");
        assertThat(parking.getPricePerHour()).isEqualByComparingTo("100");
        assertThat(parking.getVehicleType()).isEqualTo("CAR");
        assertThat(parking.getStatus()).isEqualTo(ParkingStatus.AVAILABLE);

        parking.setParkingId(2L);
        parking.setOwnerId(20L);
        parking.setLocation("Mumbai");
        parking.setAddress("MG Road");
        parking.setPricePerHour(BigDecimal.valueOf(150));
        parking.setVehicleType("BIKE");
        parking.setStatus(ParkingStatus.UNAVAILABLE);

        assertThat(parking.getParkingId()).isEqualTo(2L);
        assertThat(parking.getOwnerId()).isEqualTo(20L);
        assertThat(parking.getLocation()).isEqualTo("Mumbai");
        assertThat(parking.getAddress()).isEqualTo("MG Road");
        assertThat(parking.getPricePerHour()).isEqualByComparingTo("150");
        assertThat(parking.getVehicleType()).isEqualTo("BIKE");
        assertThat(parking.getStatus()).isEqualTo(ParkingStatus.UNAVAILABLE);
    }

    @Test
    void parking_builderEqualsHashCodeAndToString_work() {
        Parking parking1 = Parking.builder()
                .parkingId(1L)
                .ownerId(10L)
                .location("Pune")
                .address("Main Road")
                .pricePerHour(BigDecimal.valueOf(100))
                .vehicleType("CAR")
                .status(ParkingStatus.AVAILABLE)
                .build();

        Parking parking2 = Parking.builder()
                .parkingId(1L)
                .ownerId(10L)
                .location("Pune")
                .address("Main Road")
                .pricePerHour(BigDecimal.valueOf(100))
                .vehicleType("CAR")
                .status(ParkingStatus.AVAILABLE)
                .build();

        assertThat(parking1).isEqualTo(parking2);
        assertThat(parking1.hashCode()).isEqualTo(parking2.hashCode());
        assertThat(parking1.toString()).contains("Pune");
    }

    @Test
    void parking_noArgsConstructor_works() {
        Parking parking = new Parking();

        assertThat(parking).isNotNull();
        assertThat(parking.getParkingId()).isNull();
        assertThat(parking.getOwnerId()).isNull();
        assertThat(parking.getLocation()).isNull();
        assertThat(parking.getAddress()).isNull();
        assertThat(parking.getPricePerHour()).isNull();
        assertThat(parking.getVehicleType()).isNull();
        assertThat(parking.getStatus()).isNull();
    }

    @Test
    void parkingStatus_containsExpectedValues() {
        assertThat(ParkingStatus.values())
                .containsExactly(
                        ParkingStatus.AVAILABLE,
                        ParkingStatus.UNAVAILABLE
                );

        assertThat(ParkingStatus.valueOf("AVAILABLE"))
                .isEqualTo(ParkingStatus.AVAILABLE);

        assertThat(ParkingStatus.valueOf("UNAVAILABLE"))
                .isEqualTo(ParkingStatus.UNAVAILABLE);
    }
}