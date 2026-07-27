package by.shakhau.ps.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@EqualsAndHashCode(of = { "userId" }, callSuper = false)
public class UserShortCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    private String passwordHash;
    private Boolean passwordActive;
}
