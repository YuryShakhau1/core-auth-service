package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.model.Role;
import by.shakhau.ps.auth.repository.RoleRepository;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void shouldReturnAllRoleNamesWhenFindAllRoleNamesIsCalled() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        when(repository.findAllRoleNames()).thenReturn(roles);

        List<String> result = service.findAllRoleNames();

        assertEquals(roles, result);
        verify(repository).findAllRoleNames();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnAllRolesWhenFindAllRoleNamesIsCalled() {
        List<Role> roles = List.of(
                new Role(1L, "ROLE_USER"), new Role(2L, "ROLE_ADMIN"));

        when(repository.findAll()).thenReturn(roles);

        List<Role> result = service.findAll();

        assertEquals(roles, result);
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnUserRolesWhenFindByUserIdIsCalled() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_USER");

        when(repository.findNamesByUserId(userId)).thenReturn(roles);

        List<String> result = service.findNamesByUserId(userId);

        assertEquals(roles, result);
        verify(repository).findNamesByUserId(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldReturnRoleIdWhenRoleExists() {
        String roleName = "ROLE_ADMIN";
        Long roleId = 2L;

        when(repository.findIdByName(roleName)).thenReturn(Optional.of(roleId));

        Long result = service.findIdByName(roleName);

        assertEquals(roleId, result);
        verify(repository).findIdByName(roleName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenRoleDoesNotExist() {
        String roleName = "ROLE_UNKNOWN";

        when(repository.findIdByName(roleName)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.findIdByName(roleName));

        assertEquals(
                "Role name %s not found".formatted(roleName),
                exception.getMessage());

        verify(repository).findIdByName(roleName);
        verifyNoMoreInteractions(repository);
    }
}
