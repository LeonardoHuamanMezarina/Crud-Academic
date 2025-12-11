package pe.edu.vallegrande.arquitectura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import pe.edu.vallegrande.arquitectura.model.User;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(Character status);
    List<User> findByRoles(String roles);
    List<User> findByRolesOrderByRegistrationDateDesc(String roles);

    Optional<User> findByUsername(String username);
}
