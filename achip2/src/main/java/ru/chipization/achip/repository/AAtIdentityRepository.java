package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import ru.chipization.achip.model.AAtIdentity;
import ru.chipization.achip.model.Animal;
import ru.chipization.achip.model.AnimalType;

public interface AAtIdentityRepository extends JpaRepository<AAtIdentity, Long> {
    Optional<List<AAtIdentity>> findAAtIdentitiesByIdAnimal(Animal animal);
    Optional<AAtIdentity> findAAtIdentitiesByIdAnimalAndAndIdAnimalType(Animal animal, AnimalType animalType);
}
