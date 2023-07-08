package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.chipization.achip.model.Animal;
import ru.chipization.achip.model.VisitedLocation;

import java.util.List;
import java.util.Optional;

public interface VisitedLocationRepository extends JpaRepository<VisitedLocation, Long>, JpaSpecificationExecutor<VisitedLocation> {
    Optional<List<VisitedLocation>> findVisitedLocationByIdAnimal(Animal animal);
    Optional<VisitedLocation> findVisitedLocationByIdChippingLocation_LongitudeAndIdChippingLocation_Latitude(Double longitude, Double latitude);
}
