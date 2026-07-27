package by.shakhau.ps.auth.messaging.event;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserCreatedEvent {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String tempPassword;
    private String role;
    private Boolean active;
}
