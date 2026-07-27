package by.shakhau.ps.auth.messaging.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserRegisteredEvent {

    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private Boolean active;
}
