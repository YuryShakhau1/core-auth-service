package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.controller.dto.request.ChangePasswordRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateFirstAdminRequest;
import by.shakhau.ps.auth.controller.dto.request.CreateUserRequest;
import by.shakhau.ps.auth.controller.dto.response.UserResponse;
import by.shakhau.ps.auth.controller.dto.response.UserRoleNameResponse;
import by.shakhau.ps.auth.controller.exception.CustomValidationException;
import by.shakhau.ps.auth.controller.exception.UnauthorizedException;
import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.impl.JwtService;
import by.shakhau.ps.auth.service.model.UserInfo;
import by.shakhau.ps.auth.util.PasswordUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

import static by.shakhau.ps.auth.model.Role.ROLE_ADMIN;
import static by.shakhau.ps.auth.model.Role.ROLE_USER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth/users")
@AllArgsConstructor
public class UserCredentialController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialMapper mapper;
    private final UserRoleService userRoleService;
    private final UserCredentialService service;
    private final SecurityProps securityProps;

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> findCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.substring("Bearer ".length());
        Claims claims = jwtService.getClaims(accessToken);
        UUID userId = UUID.fromString(claims.getSubject());
        UserCredential userCredential = service.findByUserId(userId);
        return ResponseEntity.ok(mapper.toGetUserResponse(userCredential));
    }

    @GetMapping(value = "/me/roles", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRoleNameResponse> findCurrentUserRoles(
            @RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.substring("Bearer ".length());
        Claims claims = jwtService.getClaims(accessToken);
        UUID userId = UUID.fromString(claims.getSubject());
        return ResponseEntity.ok(new UserRoleNameResponse(userRoleService.findUserRoles(userId)));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return createUser(request, ROLE_USER);
    }

    @PostMapping(value = "/create-admin", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateFirstAdminRequest request) {
        if (!securityProps.getAdminInitSecretHash().equals(DigestUtils.sha256Hex(request.getAdminInitSecret()))) {
            throw new UnauthorizedException("Wrong adminInitSecret = %s".formatted(request.getAdminInitSecret()));
        }

        return createUser(request, ROLE_ADMIN);
    }

    @PatchMapping(value = "/change-password", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (!Arrays.equals(request.getPassword(), request.getRepeatPassword())) {
            throw new CustomValidationException("Password and repeat password must match");
        }

        if (Arrays.equals(request.getPassword(), request.getNewPassword())) {
            throw new CustomValidationException("Password and new password must be different");
        }

        Arrays.fill(request.getRepeatPassword(), '0');
        request.setRepeatPassword(null);

        UserShortCredential userCredential = service.findByEmail(request.getEmail());
        var password = new StringBuilder().append(request.getPassword());
        if (userCredential == null || !passwordEncoder.matches(password, userCredential.getPasswordHash())) {
            PasswordUtil.clearPassword(request, password);
            throw new ResourceForbiddenException("Wrong email or password");
        }

        service.updatePassword(userCredential.getUserId(), request.getNewPassword());

        Arrays.fill(request.getNewPassword(), '0');
        request.setNewPassword(null);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<UserResponse> createUser(CreateUserRequest request, String role) {
        if (!Arrays.equals(request.getPassword(), request.getRepeatPassword())) {
            throw new CustomValidationException("Password and repeat password must match");
        }

        Arrays.fill(request.getRepeatPassword(), '0');
        request.setRepeatPassword(null);
        UserInfo userInfo = mapper.toUserInfo(request);
        userInfo.setPasswordActive(true);
        UserCredential userCredential = service.registerUser(userInfo, role);
        PasswordUtil.clearPassword(request);

        return ResponseEntity.ok(mapper.toGetUserResponse(userCredential));
    }
}
