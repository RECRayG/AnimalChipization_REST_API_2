package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.chipization.achip.model.Areas;

import java.util.List;
import java.util.Optional;

public interface AreasRepository extends JpaRepository<Areas, Long> {
    Boolean existsAreasByName(String name);

    @Query("SELECT DISTINCT A " +
            "FROM Areas A " +
            "INNER JOIN A.aAlIdentityCollection AS AAL " +
            "INNER JOIN AAL.idAreaLocation AS AL " +
            "WHERE AL.longitude = ?1 AND AL.latitude = ?2")
    Optional<List<Areas>> FindAllAreasByContainsPoint(Double longitude, Double latitude);

    @Query("SELECT DISTINCT A " +
            "FROM Areas A " +
            "INNER JOIN A.aAlIdentityCollection AS AAL " +
            "INNER JOIN AAL.idAreaLocation AS AL " +
            "WHERE (AL.longitude between ?1 and ?2) or (AL.latitude between ?3 and ?4)")
    Optional<List<Areas>> FindAllAreasByProjectionXY(Double minLongitude, Double maxLongitude,
                                                     Double minLatitude, Double maxLatitude);
}
