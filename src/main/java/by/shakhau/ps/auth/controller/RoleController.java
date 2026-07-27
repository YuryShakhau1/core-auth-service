package by.shakhau.ps.auth.controller;

import by.shakhau.ps.auth.controller.dto.response.UserRoleNameResponse;
import by.shakhau.ps.auth.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth/roles")
@AllArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRoleNameResponse> findAllRoles() {
        return ResponseEntity.ok(new UserRoleNameResponse(service.findAll()));
    }
}
