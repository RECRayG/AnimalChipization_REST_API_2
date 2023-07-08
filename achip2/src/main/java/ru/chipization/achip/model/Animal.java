package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
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
@Table(name = "animals", schema = "public", catalog = "animals_chipization")
public class Animal implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_animal", nullable = false)
    private Long id;

    @Basic
    @Column(name = "weight", nullable = false)
    private Float weight;

    @Basic
    @Column(name = "length", nullable = false)
    private Float length;

    @Basic
    @Column(name = "height", nullable = false)
    private Float height;

    @ManyToOne//(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_animal_gender", referencedColumnName = "id_animal_gender", nullable = false)
    private AnimalGender idAnimalGender;

    @ManyToOne//(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_animal_life_status", referencedColumnName = "id_animal_life_status", nullable = false)
    private AnimalsLifeStatus idAnimalLifeStatus;

    @Basic
    @Column(name = "chipping_date_time", nullable = false)
    private Instant chippingDateTime;

    @ManyToOne//(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_chipper", referencedColumnName = "id_user", nullable = false)
    private User idChipper;

    @ManyToOne//(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_chipping_location", referencedColumnName = "id_chipping_location", nullable = false)
    private ChippingLocation idChippingLocation;

    @OneToMany(mappedBy = "idAnimal", cascade = CascadeType.ALL, orphanRemoval = false)
    private Collection<AAtIdentity> aAtIdentityCollection;

    @OneToMany(mappedBy = "idAnimal", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private Collection<VisitedLocation> visitedLocationCollection;

    @Basic
    @Column(name = "death_date_time")
    private Instant deathDateTime;
}