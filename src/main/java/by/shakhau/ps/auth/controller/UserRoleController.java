package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.controller.dto.request.AddUserRolesRequest;
import by.shakhau.ps.auth.controller.dto.response.UserRoleNameResponse;
import by.shakhau.ps.auth.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth/users/{userId}/roles")
@AllArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRoleNameResponse> findUserRoles(@PathVariable UUID userId) {
        return ResponseEntity.ok(new UserRoleNameResponse(userRoleService.findUserRoles(userId)));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AddUserRolesRequest request) {
        userRoleService.addUserRoles(userId, request.getRoleNames());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable UUID userId, @PathVariable String roleName) {
        userRoleService.deleteUserRole(userId, roleName);
        return ResponseEntity.noContent().build();
    }
}
