package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Collection;

@Data
@ToString
@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chipping_locations", schema = "public", catalog = "animals_chipization")
public class ChippingLocation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chipping_location", nullable = false)
    private Long id;

    @Basic
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Basic
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @OneToMany(mappedBy = "idChippingLocation", cascade = CascadeType.ALL, orphanRemoval = false)
    private Collection<Animal> animalCollection;

    @OneToMany(mappedBy = "idChippingLocation", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    private Collection<VisitedLocation> visitedLocationCollection;
}