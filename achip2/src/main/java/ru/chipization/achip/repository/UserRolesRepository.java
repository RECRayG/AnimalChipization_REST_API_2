package ru.chipization.achip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chipization.achip.model.UserRoles;

import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRoles, Long> {
    Optional<UserRoles> findUserRolesByRole(String role);
    Boolean existsUserRolesByRole(String role);
}
