package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.AnimalsLifeStatus;

import java.util.Optional;

public interface AnimalLifeStatusRepository extends JpaRepository<AnimalsLifeStatus, Long> {
    Optional<AnimalsLifeStatus> findAnimalsLifeStatusByLifeStatus(String lifeStatus);
}
