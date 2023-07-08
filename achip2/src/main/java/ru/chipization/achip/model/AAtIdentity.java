package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Data
@ToString
@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "a_at_identity", schema = "public", catalog = "animals_chipization")
public class AAtIdentity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_a_at_identity")
    private Long id;

    @ManyToOne//(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_animal", referencedColumnName = "id_animal", nullable = false)
    private Animal idAnimal;

    @ManyToOne//(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_animal_type", referencedColumnName = "id_animal_type", nullable = false)
    private AnimalType idAnimalType;
}