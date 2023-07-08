package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.ChippingLocation;

import java.util.Optional;

public interface ChippingLocationRepository extends JpaRepository<ChippingLocation, Long> {
    Optional<ChippingLocation> findChippingLocationByLongitudeAndLatitude(Double longitude, Double latitude);
}
