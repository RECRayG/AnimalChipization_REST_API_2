package ru.chipization.achip.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@ToString
@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "a_al_identity", schema = "public", catalog = "animals_chipization")
public class AAlIdentity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_a_al_identity")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_area", referencedColumnName = "id_area", nullable = false)
    private Areas idArea;

    @ManyToOne
    @JoinColumn(name = "id_area_location", referencedColumnName = "id_area_location", nullable = false)
    private AreaLocations idAreaLocation;
}
