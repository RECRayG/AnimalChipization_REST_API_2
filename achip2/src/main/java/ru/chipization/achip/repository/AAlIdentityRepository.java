package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.AAlIdentity;
import ru.chipization.achip.model.Areas;

import java.util.List;
import java.util.Optional;

public interface AAlIdentityRepository extends JpaRepository<AAlIdentity, Long> {
    Optional<List<AAlIdentity>> findAAlIdentitiesByIdArea(Areas area);
}
