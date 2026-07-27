package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private RoleService roleService;

    @Mock
    private UserCredentialRepository repository;

    @InjectMocks
    private UserRoleServiceImpl service;

    @Test
    void shouldReturnUserRolesWhenFindUserRolesIsCalled() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(roleService.findByUserId(userId)).thenReturn(roles);

        List<String> result = service.findUserRoles(userId);

        assertEquals(roles, result);
        verify(roleService).findByUserId(userId);
    }

    @Test
    void shouldAddUserRoleWhenRoleIdIsProvided() {
        UUID userId = UUID.randomUUID();
        Long roleId = 1L;

        service.addUserRole(userId, roleId);

        verify(repository).addUserRole(userId, roleId);
        verifyNoInteractions(roleService);
    }

    @Test
    void shouldAddUserRoleWhenRoleNameIsProvided() {
        UUID userId = UUID.randomUUID();

        String roleName = "ROLE_USER";
        Long roleId = 1L;

        when(roleService.findIdByName(roleName)).thenReturn(roleId);

        service.addUserRole(userId, roleName);

        verify(roleService).findIdByName(roleName);
        verify(repository).addUserRole(userId, roleId);
    }


    @Test
    void shouldAddAllUserRolesWhenRoleNamesAreProvided() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(roleService.findIdByName("ROLE_USER")).thenReturn(1L);
        when(roleService.findIdByName("ROLE_ADMIN")).thenReturn(2L);

        service.addUserRoles(userId, roles);

        verify(roleService).findIdByName("ROLE_USER");
        verify(roleService).findIdByName("ROLE_ADMIN");
        verify(repository).addUserRole(userId, 1L);
        verify(repository).addUserRole(userId, 2L);
    }

    @Test
    void shouldDeleteUserRoleWhenRoleNameIsProvided() {
        UUID userId = UUID.randomUUID();
        String roleName = "ROLE_ADMIN";
        Long roleId = 2L;

        when(roleService.findIdByName(roleName)).thenReturn(roleId);

        service.deleteUserRole(userId, roleName);

        verify(roleService).findIdByName(roleName);
        verify(repository).deleteUserRole(userId, roleId);
    }
}
