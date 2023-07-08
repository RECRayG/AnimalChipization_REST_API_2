package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@ToString
@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visited_locations", schema = "public", catalog = "animals_chipization")
public class VisitedLocation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visited_location", nullable = false)
    private Long id;

    @Basic
    @Column(name = "date_time_of_visit_location_point", nullable = false)
    private Instant dateTimeOfVisitLocationPoint;

    @ManyToOne( fetch = FetchType.EAGER, optional = false )//(fetch = FetchType.EAGER/*, optional = false*/)
    @JoinColumn(name = "id_chipping_location", referencedColumnName = "id_chipping_location", nullable = false)
    private ChippingLocation idChippingLocation;

    @ManyToOne( fetch = FetchType.EAGER, optional = false )//(fetch = FetchType.EAGER/*, optional = false*/)
    @JoinColumn(name = "id_animal", referencedColumnName = "id_animal", nullable = false)
    private Animal idAnimal;
}