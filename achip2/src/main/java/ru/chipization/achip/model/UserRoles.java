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
@Table(name = "user_roles", schema = "public", catalog = "animals_chipization")
public class UserRoles implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_user_role")
    private int idUserRole;

    @Basic
    @Column(name = "role")
    private String role;

    @OneToMany(mappedBy = "userRolesByIdUserRole", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<User> usersByIdUserRole;
}
