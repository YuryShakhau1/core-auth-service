package by.shakhau.ps.auth.controller.dto.request;

import by.shakhau.ps.auth.model.Password;
import by.shakhau.ps.auth.model.serialization.SafePasswordDeserializer;
import by.shakhau.ps.auth.model.serialization.SafePasswordSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserRequest implements Password {

    @NotBlank(message = "User first name is required")
    @Size(min = 1, max = 50, message = "User first name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "User last name must be between 1 and 50 characters")
    private String lastName;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be a date in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotEmpty(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = """
                      Password must contain at least one lowercase letter,
                      one uppercase letter,
                      one digit,
                      and one special character
                      """
    )
    @Size(min = 7, max = 100, message = "Password must be between 7 and 100 characters")
    @JsonDeserialize(using = SafePasswordDeserializer.class)
    @JsonSerialize(using = SafePasswordSerializer.class)
    private StringBuilder password;

    @NotEmpty(message = "Repeat password is required")
    @JsonDeserialize(using = SafePasswordDeserializer.class)
    @JsonSerialize(using = SafePasswordSerializer.class)
    private StringBuilder repeatPassword;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
