package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import ru.chipization.achip.model.Animal;

public interface AnimalRepository extends JpaRepository<Animal,Long>, JpaSpecificationExecutor<Animal> {
    Optional<Animal> findAnimalByIdChippingLocation_LongitudeAndIdChippingLocation_Latitude(Double longitude, Double latitude);
}
