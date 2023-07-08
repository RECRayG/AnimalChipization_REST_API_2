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
@Table(name = "areas", schema = "public", catalog = "animals_chipization")
public class Areas {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_area")
    private Long idArea;

    @Basic
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "idArea", cascade = CascadeType.ALL, orphanRemoval = false)
    private Collection<AAlIdentity> aAlIdentityCollection;
}
