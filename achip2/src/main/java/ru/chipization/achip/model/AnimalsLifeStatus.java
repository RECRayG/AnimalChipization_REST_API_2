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
@Table(name = "animals_life_status", schema = "public", catalog = "animals_chipization")
public class AnimalsLifeStatus implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal_life_status", nullable = false)
    private Long id;

    @Basic
    @Column(name = "life_status", nullable = false)
    private String lifeStatus;

    @OneToMany(mappedBy = "idAnimalLifeStatus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Animal> animalCollection;
}