package by.shakhau.ps.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Boolean active) {
}
