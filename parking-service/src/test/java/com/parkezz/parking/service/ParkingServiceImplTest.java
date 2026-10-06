package com.parkezz.parking.service;

import com.parkezz.parking.dto.ParkingRequest;
import com.parkezz.parking.dto.ParkingResponse;
import com.parkezz.parking.entity.Parking;
import com.parkezz.parking.entity.ParkingStatus;
import com.parkezz.parking.exception.ParkingNotFoundException;
import com.parkezz.parking.exception.UnauthorizedActionException;
import com.parkezz.parking.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceImplTest {

    @Mock
    private ParkingRepository parkingRepository;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    private ParkingRequest request;
    private Parking existingParking;

    @BeforeEach
    void setUp() {
        request = new ParkingRequest("Downtown", "123 Main St", BigDecimal.valueOf(50), "CAR", ParkingStatus.AVAILABLE);
        existingParking = Parking.builder()
                .parkingId(1L).ownerId(10L).location("Downtown").address("123 Main St")
                .pricePerHour(BigDecimal.valueOf(50)).vehicleType("CAR").status(ParkingStatus.AVAILABLE)
                .build();
    }

    @Test
    void create_validRequest_returnsParkingResponse() {
        when(parkingRepository.save(any(Parking.class))).thenAnswer(inv -> {
            Parking p = inv.getArgument(0);
            p.setParkingId(1L);
            return p;
        });

        ParkingResponse response = parkingService.create(request, 10L);

        assertThat(response.getParkingId()).isEqualTo(1L);
        assertThat(response.getOwnerId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.AVAILABLE);
    }

    @Test
    void getById_notFound_throwsParkingNotFoundException() {
        when(parkingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.getById(99L))
                .isInstanceOf(ParkingNotFoundException.class);
    }

    @Test
    void getById_found_returnsParkingResponse() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(existingParking));

        ParkingResponse response = parkingService.getById(1L);

        assertThat(response.getParkingId()).isEqualTo(1L);
        assertThat(response.getLocation()).isEqualTo("Downtown");
    }

    @Test
    void getAll_returnsAllParkingSpaces() {
        when(parkingRepository.findAll()).thenReturn(java.util.List.of(existingParking));

        java.util.List<ParkingResponse> responses = parkingService.getAll();

        assertThat(responses).hasSize(1);
    }

    @Test
    void search_delegatesToRepositoryAndMapsResults() {
        when(parkingRepository.search("Downtown", "CAR", ParkingStatus.AVAILABLE))
                .thenReturn(java.util.List.of(existingParking));

        java.util.List<ParkingResponse> responses = parkingService.search("Downtown", "CAR", ParkingStatus.AVAILABLE);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getLocation()).isEqualTo("Downtown");
    }

    @Test
    void search_noMatches_returnsEmptyList() {
        when(parkingRepository.search(any(), any(), any())).thenReturn(java.util.Collections.emptyList());

        java.util.List<ParkingResponse> responses = parkingService.search("Nowhere", null, null);

        assertThat(responses).isEmpty();
    }

    @Test
    void update_notFound_throwsParkingNotFoundException() {
        when(parkingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.update(99L, request, 10L))
                .isInstanceOf(ParkingNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsParkingNotFoundException() {
        when(parkingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.delete(99L, 10L))
                .isInstanceOf(ParkingNotFoundException.class);
    }

    @Test
    void update_byOwner_succeeds() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(existingParking));
        when(parkingRepository.save(any(Parking.class))).thenAnswer(inv -> inv.getArgument(0));

        ParkingRequest updateReq = new ParkingRequest(
                "Uptown", "456 New St", BigDecimal.valueOf(75), "BIKE", ParkingStatus.UNAVAILABLE);

        ParkingResponse response = parkingService.update(1L, updateReq, 10L);

        assertThat(response.getLocation()).isEqualTo("Uptown");
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.UNAVAILABLE);
    }

    @Test
    void update_byNonOwner_throwsUnauthorizedActionException() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(existingParking));

        assertThatThrownBy(() -> parkingService.update(1L, request, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void delete_byNonOwner_throwsUnauthorizedActionException() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(existingParking));

        assertThatThrownBy(() -> parkingService.delete(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void delete_byOwner_succeeds() {
        when(parkingRepository.findById(1L)).thenReturn(Optional.of(existingParking));

        parkingService.delete(1L, 10L);

        org.mockito.Mockito.verify(parkingRepository).delete(existingParking);
    }
}
