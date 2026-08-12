package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.dto.request.ChangePasswordRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateFirstAdminRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateUserRequest;
import by.shakhau.ps.auth.controller.dto.response.UserResponse;
import by.shakhau.ps.auth.controller.dto.response.UserRoleNameResponse;
import by.shakhau.ps.auth.controller.exception.CustomValidationException;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.model.UserInfo;
import by.shakhau.ps.auth.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static by.shakhau.ps.auth.model.Role.ROLE_ADMIN;
import static by.shakhau.ps.auth.model.Role.ROLE_USER;
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

    @GetMapping(value = "/me/roles", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRoleNameResponse> findCurrentUserRoles(
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(new UserRoleNameResponse(userRoleService.findUserRoles(userId)));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody CreateUserRequest request) {
        return registerUser(request, ROLE_USER);
    }

    @PostMapping(value = "/create-admin", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateFirstAdminRequest request) {
        if (!securityProps.getAdminInitSecretHash().equals(DigestUtils.sha256Hex(request.getAdminInitSecret()))) {
            throw new UnauthorizedException("Wrong adminInitSecret");
        }

        return registerUser(request, ROLE_ADMIN);
    }

    @PatchMapping(value = "/change-password", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!PasswordUtil.compare(request.getPassword(), request.getRepeatPassword())) {
            throw new CustomValidationException("Password and repeat password must match");
        }

        if (PasswordUtil.compare(request.getPassword(), request.getNewPassword())) {
            throw new CustomValidationException("Password and new password must be different");
        }

        PasswordUtil.clearPassword(request.getRepeatPassword());
        request.setRepeatPassword(null);

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

    private ResponseEntity<UserResponse> registerUser(CreateUserRequest request, String role) {
        if (!PasswordUtil.compare(request.getPassword(), request.getRepeatPassword())) {
            throw new CustomValidationException("Password and repeat password must match");
        }

        PasswordUtil.clearPassword(request.getRepeatPassword());
        request.setRepeatPassword(null);
        UserInfo userInfo = mapper.toUserInfo(request);
        userInfo.setPasswordActive(true);
        UserCredential userCredential = service.registerUser(userInfo, role);
        PasswordUtil.clearPassword(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toGetUserResponse(userCredential));
    }
}
