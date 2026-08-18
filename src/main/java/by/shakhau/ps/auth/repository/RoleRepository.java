package by.shakhau.ps.auth.repository;

import by.shakhau.ps.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query(value = """
            SELECT r.name FROM roles r
            """, nativeQuery = true)
    List<String> findAllRoleNames();

    @Query(value = """
            SELECT r.name FROM roles r 
            INNER JOIN user_credential_roles ur ON r.id = ur.role_id 
            WHERE ur.user_id = :userId
            """, nativeQuery = true)
    List<String> findNamesByUserId(UUID userId);

    @Query(value = """
            SELECT r.id, r.name FROM roles r 
            INNER JOIN user_credential_roles ur ON r.id = ur.role_id 
            WHERE ur.user_id = :userId
            """, nativeQuery = true)
    List<Role> findByUserId(UUID userId);

    @Query("SELECT r.id FROM Role r WHERE r.name = :name")
    Optional<Long> findIdByName(String name);
}
