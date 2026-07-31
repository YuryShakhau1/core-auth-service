package by.shakhau.ps.auth.controller.dto.request;

import by.shakhau.ps.auth.model.Password;
import by.shakhau.ps.auth.model.serialization.SafePasswordDeserializer;
import by.shakhau.ps.auth.model.serialization.SafePasswordSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest implements Password {

    public String email;

    @NotEmpty(message = "Password is required")
    @JsonDeserialize(using = SafePasswordDeserializer.class)
    @JsonSerialize(using = SafePasswordSerializer.class)
    public StringBuilder password;
}