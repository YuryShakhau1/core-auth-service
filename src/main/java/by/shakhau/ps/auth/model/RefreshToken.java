package by.shakhau.ps.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@EqualsAndHashCode(of = { "userId", "sessionId" }, callSuper = false)
public class RefreshToken {

    private UUID userId;

    @Id
    private UUID sessionId;

    private String tokenHash;
    private Date createdAt;
    private Date expiryDate;
}
