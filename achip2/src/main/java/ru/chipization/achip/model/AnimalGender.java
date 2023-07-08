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
@Table(name = "animal_genders", schema = "public", catalog = "animals_chipization")
public class AnimalGender implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal_gender", nullable = false)
    private Long id;

    @Basic
    @Column(name = "gender", nullable = false)
    private String gender;

    @OneToMany(mappedBy = "idAnimalGender", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Animal> animalCollection;
}