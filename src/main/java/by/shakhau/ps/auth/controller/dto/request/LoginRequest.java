package by.shakhau.ps.auth.controller.dto.request;

import by.shakhau.ps.auth.model.Password;
import by.shakhau.ps.auth.util.SafePasswordDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest implements Password {

    public String email;

    @NotEmpty(message = "Password is required")
    @JsonDeserialize(using = SafePasswordDeserializer.class)
    public char[] password;
}