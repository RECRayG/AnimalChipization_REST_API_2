package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "animal_types", schema = "public", catalog = "animals_chipization")
public class AnimalType {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_animal_type", nullable = false)
    private Long id;

    @Basic
    @Column(name = "type", nullable = false)
    private String type;

    // fetch = FetchType.EAGER, cascade = CascadeType.ALL
    @OneToMany(mappedBy = "idAnimalType", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    private Collection<AAtIdentity> aAtIdentities;
}