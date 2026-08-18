package by.shakhau.ps.auth.repository;

import by.shakhau.ps.auth.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    @Query("""
            UPDATE UserCredential uc SET uc.passwordHash = :passwordHash, uc.passwordActive = TRUE 
            WHERE uc.userId = :userId
            """)
    @Modifying
    void updatePassword(UUID userId, String passwordHash);

    @Query("""
            UPDATE UserCredential uc SET uc.active = :active 
            WHERE uc.userId = :userId
            """)
    @Modifying
    void updateActive(UUID userId, Boolean active);

    @Modifying
    @Query(value = """
            INSERT INTO user_credentials (user_id, first_name, last_name, email, password_hash, password_active, active, created_at, updated_at) 
            SELECT :#{#u.userId}, :#{#u.firstName}, :#{#u.lastName}, :#{#u.email}, :#{#u.passwordHash}, :#{#u.passwordActive}, :#{#u.active}, NOW(), NOW() 
            WHERE NOT EXISTS (SELECT 1 FROM user_credentials WHERE email = :#{#u.email} OR user_id = :#{#u.userId})
            """,
            nativeQuery = true)
    void insertIfDoesNotExist(@Param("u") UserCredential userCredential);

    @Modifying
    @Query(value = """
            INSERT INTO user_credential_roles (user_id, role_id) VALUES (:userId, :roleId)
            ON CONFLICT (user_id, role_id) DO NOTHING
            """,
            nativeQuery = true
    )
    void addUserRole(UUID userId, Long roleId);

    @Modifying
    @Query(value = """
            DELETE FROM user_credential_roles WHERE user_id = :userId AND role_id = :roleId
            """,
            nativeQuery = true
    )
    void deleteUserRole(UUID userId, Long roleId);

    @Modifying
    @Query(value = """
            DELETE FROM user_credential_roles WHERE user_id = :userId AND role_id IN :roleIds
            """,
            nativeQuery = true
    )
    void deleteUserRoles(UUID userId, Collection<Long> roleIds);
}
