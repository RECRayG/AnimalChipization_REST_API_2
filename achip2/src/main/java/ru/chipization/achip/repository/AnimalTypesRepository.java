package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.AnimalType;

import java.util.Optional;

public interface AnimalTypesRepository extends JpaRepository<AnimalType,Long> {
    Optional<AnimalType> findByType(String type);
}
