package by.shakhau.ps.auth.controller.dto.request;

import by.shakhau.ps.auth.model.Password;
import by.shakhau.ps.auth.util.SafePasswordDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest implements Password {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
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
    private char[] password;

    @NotBlank(message = "Repeat password is required")
    @JsonDeserialize(using = SafePasswordDeserializer.class)
    private char[] repeatPassword;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = """
                      New password must contain at least one lowercase letter, 
                      one uppercase letter, 
                      one digit, 
                      and one special character
                      """
    )
    @Size(min = 7, max = 100, message = "New password must be between 7 and 100 characters")
    private char[] newPassword;
}
