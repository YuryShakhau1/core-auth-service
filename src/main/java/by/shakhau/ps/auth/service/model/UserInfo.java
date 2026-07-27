package by.shakhau.ps.auth.service.model;

import by.shakhau.ps.auth.model.Password;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserInfo implements Password {

    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private char[] password;
    private Boolean passwordActive;
    private Boolean active;
}
