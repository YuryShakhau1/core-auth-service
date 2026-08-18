package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.dto.request.ChangePasswordRequest;
import by.shakhau.ps.auth.controller.dto.response.UserResponse;
import by.shakhau.ps.auth.controller.dto.response.UserRoleNameResponse;
import by.shakhau.ps.auth.controller.exception.CustomValidationException;
import by.shakhau.ps.auth.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth/users")
@AllArgsConstructor
public class UserCredentialController {

    private final PasswordEncoder passwordEncoder;
    private final UserCredentialMapper mapper;
    private final UserRoleService userRoleService;
    private final UserCredentialService service;
    private final SecurityProps securityProps;

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> findCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        UserCredential userCredential = service.findByUserId(userId);
        return ResponseEntity.ok(mapper.toGetUserResponse(userCredential));
    }

    @PatchMapping(value = "/change-password", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!PasswordUtil.equals(request.getNewPassword(), request.getRepeatNewPassword())) {
            throw new CustomValidationException("New password and repeat password must match");
        }

        if (PasswordUtil.equals(request.getPassword(), request.getNewPassword())) {
            throw new CustomValidationException("Password and new password must be different");
        }

        PasswordUtil.clearPassword(request.getRepeatNewPassword());
        request.setRepeatNewPassword(null);

        UserShortCredential userCredential = service.findByEmail(request.getEmail());
        var password = new StringBuilder().append(request.getPassword());
        if (userCredential == null || !passwordEncoder.matches(password, userCredential.getPasswordHash())) {
            PasswordUtil.clearPassword(request, password);
            throw new ResourceForbiddenException("Wrong email or password");
        }

        service.updatePassword(userCredential.getUserId(), request.getNewPassword());

        PasswordUtil.clearPassword(request.getNewPassword());
        request.setNewPassword(null);

        return ResponseEntity.noContent().build();
    }
}
