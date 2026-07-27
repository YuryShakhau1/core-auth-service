package by.shakhau.ps.auth.repository;

import by.shakhau.ps.auth.model.UserShortCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserShortCredentialRepository extends JpaRepository<UserShortCredential, UUID> {

    @Query(
            value = """
                    SELECT user_id, password_hash, password_active FROM user_credentials 
                    WHERE email = :email AND active = TRUE
                    """,
            nativeQuery = true)
    Optional<UserShortCredential> findByEmail(String email);
}
