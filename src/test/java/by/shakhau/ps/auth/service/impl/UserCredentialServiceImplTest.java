package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.messaging.mapper.UserEventMapper;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.repository.UserShortCredentialRepository;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import by.shakhau.ps.auth.service.model.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserEventMapper userEventMapper;

    @Mock
    private UserCredentialMapper mapper;

    @Mock
    private UserCredentialRepository repository;

    @Mock
    private UserShortCredentialRepository shortRepository;

    @InjectMocks
    private UserCredentialServiceImpl service;

    @Test
    void shouldReturnUserShortCredentialWhenEmailExists() {
        String email = "user@test.com";

        UserShortCredential credential = new UserShortCredential();

        when(shortRepository.findByEmail(email)).thenReturn(Optional.of(credential));

        UserShortCredential result = service.findByEmail(email);

        assertEquals(credential, result);

        verify(shortRepository).findByEmail(email);
    }

    @Test
    void shouldReturnNullEmailDoesNotExist() {
        String email = "unknown@test.com";

        when(shortRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserShortCredential result = service.findByEmail(email);

        assertNull(result);
        verify(shortRepository).findByEmail(email);
    }

    @Test
    void shouldReturnUserCredentialWhenUserExists() {
        UUID userId = UUID.randomUUID();
        UserCredential credential = new UserCredential();

        when(repository.findById(userId)).thenReturn(Optional.of(credential));

        UserCredential result = service.findByUserId(userId);

        assertEquals(credential, result);
        verify(repository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findByUserId(userId));

        assertEquals("User ID %s not found".formatted(userId), exception.getMessage());
    }

    @Test
    void shouldUpdateExistingCredentialWhenUserFromExternalSystem() {
        UUID userId = UUID.randomUUID();
        UserInfo userInfo = createUserInfo(userId);
        UserCredential newCredential = createCredential();
        UserCredential existingCredential = createCredential();

        when(mapper.toUserCredential(true, userInfo)).thenReturn(newCredential);
        when(passwordEncoder.encode(any(StringBuilder.class))).thenReturn("hash");
        when(repository.existsById(userId)).thenReturn(true);
        when(repository.findById(userId)).thenReturn(Optional.of(existingCredential));

        service.registerExternalUser(userInfo, "ROLE_USER");

        assertFalse(existingCredential.getPasswordActive());
        assertEquals("hash", existingCredential.getPasswordHash());
        verify(repository).save(existingCredential);
        verify(repository, never()).insertIfDoesNotExist(any());
    }

    @Test
    void shouldInsertCredentialWhenExternalUserIdExistsButCredentialDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UserInfo userInfo = createUserInfo(userId);
        UserCredential credential = createCredential();

        when(mapper.toUserCredential(true, userInfo)).thenReturn(credential);
        when(passwordEncoder.encode(any(StringBuilder.class))).thenReturn("hash");
        when(repository.existsById(userId)).thenReturn(false);

        service.registerExternalUser(userInfo, "ROLE_USER");

        assertFalse(credential.getPasswordActive());
        verify(repository).insertIfDoesNotExist(credential);
        verify(repository, never()).save(any());
        verify(userRoleService).addUserRole(userId, "ROLE_USER");
    }

    @Test
    void shouldUpdateExistingCredentialWhenExternalUserAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UserInfo userInfo = createUserInfo(userId);
        UserCredential newCredential = createCredential();
        UserCredential existingCredential = createCredential();

        when(mapper.toUserCredential(true, userInfo)).thenReturn(newCredential);
        when(passwordEncoder.encode(any(StringBuilder.class))).thenReturn("hash");
        when(repository.existsById(userId)).thenReturn(true);
        when(repository.findById(userId)).thenReturn(Optional.of(existingCredential));

        service.registerExternalUser(userInfo, "ROLE_USER");

        assertFalse(existingCredential.getPasswordActive());
        assertEquals("hash", existingCredential.getPasswordHash());
        verify(repository).save(existingCredential);
        verify(repository, never()).insertIfDoesNotExist(any());
        verify(userRoleService).addUserRole(userId, "ROLE_USER");
    }

    @Test
    void shouldThrowExceptionWhenExternalUserDoesNotHaveId() {
        UserInfo userInfo = createUserInfo(null);
        UserCredential credential = createCredential();

        when(mapper.toUserCredential(true, userInfo)).thenReturn(credential);
        when(passwordEncoder.encode(any(StringBuilder.class))).thenReturn("hash");

        ResourceForbiddenException exception = assertThrows(ResourceForbiddenException.class, () ->
                service.registerExternalUser(userInfo, "ROLE_USER"));

        assertEquals("External user must have id", exception.getMessage());
        verifyNoInteractions(repository);
        verifyNoInteractions(userRoleService);
    }

    @Test
    void shouldUpdateUserWhenUserExists() {
        UUID userId = UUID.randomUUID();
        UserInfo userInfo = createUserInfo(userId);
        UserCredential credential = createCredential();

        when(repository.findById(userId)).thenReturn(Optional.of(credential));

        service.update(userInfo);

        verify(mapper).updateUserCredential(userInfo, credential);
        verify(repository).save(credential);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUnknownUser() {
        UUID userId = UUID.randomUUID();
        UserInfo userInfo = createUserInfo(userId);

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(userInfo));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdatePasswordWhenPasswordIsChanged() {
        UUID userId = UUID.randomUUID();
        StringBuilder password = new StringBuilder("password");

        when(passwordEncoder.encode(any(StringBuilder.class))).thenReturn("hash");

        service.updatePassword(userId, password);

        verify(repository).updatePassword(userId, "hash");
        verify(passwordEncoder).encode(any(StringBuilder.class));
    }

    @Test
    void shouldUpdateActiveWhenActiveStatusChanges() {
        UUID userId = UUID.randomUUID();

        service.updateActive(userId, true);

        verify(repository).updateActive(userId, true);
    }

    private UserInfo createUserInfo(UUID userId) {
        UserInfo info = new UserInfo();
        info.setUserId(userId);
        info.setFirstName("John");
        info.setLastName("Doe");
        info.setEmail("john@test.com");
        info.setPassword(new StringBuilder("password"));
        return info;
    }

    private UserCredential createCredential() {
        UserCredential credential = new UserCredential();
        credential.setFirstName("John");
        credential.setLastName("Doe");
        credential.setEmail("john@test.com");
        return credential;
    }
}
