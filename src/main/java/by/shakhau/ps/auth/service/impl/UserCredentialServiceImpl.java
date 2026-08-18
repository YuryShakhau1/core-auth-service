package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.repository.UserShortCredentialRepository;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceForbiddenException;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import by.shakhau.ps.auth.service.model.UserInfo;
import by.shakhau.ps.auth.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserCredentialServiceImpl implements UserCredentialService {

    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;
    private final UserCredentialMapper mapper;
    private final UserCredentialRepository repository;
    private final UserShortCredentialRepository shortRepository;

    @Override
    public UserShortCredential findByEmail(String email) {
        return shortRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserCredential findByUserId(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User ID %s not found".formatted(userId)));
    }

    @Transactional
    @Override
    public void registerExternalUser(UserInfo userInfo, String role) {
        UserCredential userCredential = mapper.toUserCredential(true, userInfo);
        userCredential.setPasswordHash(passwordEncoder.encode(userInfo.getPassword()));
        PasswordUtil.clearPassword(userInfo, userInfo.getPassword());

        UUID userId = userInfo.getUserId();
        if (userId != null) {
            userCredential.setPasswordActive(false);
            if (repository.existsById(userId)) {
                UserCredential credentialFound = repository.findById(userId).orElseThrow();
                credentialFound.setPasswordActive(userCredential.getPasswordActive());
                credentialFound.setPasswordHash(userCredential.getPasswordHash());
                repository.save(credentialFound);
            } else {
                repository.insertIfDoesNotExist(userCredential);
            }
        } else {
            throw new ResourceForbiddenException("External user must have id");
        }

        userRoleService.addUserRole(userId, role);
    }

    @Transactional
    @Override
    public void update(UserInfo userInfo) {
        UserCredential credential = repository.findById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserInfo with id = %s not found".formatted(userInfo.getUserId())));
        mapper.updateUserCredential(userInfo, credential);
        repository.save(credential);
    }

    @Transactional
    @Override
    public void updatePassword(UUID userId, StringBuilder password) {
        var pass = new StringBuilder().append(password);
        String passwordHash = passwordEncoder.encode(pass);
        repository.updatePassword(userId, passwordHash);
        PasswordUtil.clearPassword(pass);
    }

    @Transactional
    @Override
    public void updateActive(UUID userId, Boolean active) {
        repository.updateActive(userId, active);
    }
}
