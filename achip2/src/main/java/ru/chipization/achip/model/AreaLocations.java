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
@Table(name = "area_locations", schema = "public", catalog = "animals_chipization")
public class AreaLocations {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_area_location")
    private Long idAreaLocation;

    @Basic
    @Column(name = "longitude")
    private Double longitude;

    @Basic
    @Column(name = "latitude")
    private Double latitude;

    @OneToMany(mappedBy = "idAreaLocation", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    private Collection<AAlIdentity> aAlIdentities;
}
