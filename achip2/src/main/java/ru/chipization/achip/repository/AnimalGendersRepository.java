package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.AnimalGender;

import java.util.Optional;

public interface AnimalGendersRepository extends JpaRepository<AnimalGender, Long> {
    Optional<AnimalGender> findAnimalGenderByGender(String gender);
}
