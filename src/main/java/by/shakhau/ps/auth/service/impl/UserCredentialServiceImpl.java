package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.mapper.UserCredentialMapper;
import by.shakhau.ps.auth.messaging.event.UserRegisteredEvent;
import by.shakhau.ps.auth.messaging.mapper.UserEventMapper;
import by.shakhau.ps.auth.messaging.producer.UserRegistrationProducer;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.repository.UserShortCredentialRepository;
import by.shakhau.ps.auth.service.RoleService;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.UserRoleService;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import by.shakhau.ps.auth.service.model.UserInfo;
import by.shakhau.ps.auth.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserCredentialServiceImpl implements UserCredentialService {

    private final PasswordEncoder passwordEncoder;
    private final UserRegistrationProducer userRegistrationProducer;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final UserEventMapper userEventMapper;
    private final UserCredentialMapper mapper;
    private final UserCredentialRepository repository;
    private final UserShortCredentialRepository shortRepository;

    @Override
    public UserShortCredential findByEmail(String email) {
        return shortRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email %s not found".formatted(email)));
    }

    @Override
    public UserCredential findByUserId(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User ID %s not found".formatted(userId)));
    }

    @Transactional
    @Override
    public UserCredential registerUser(UserInfo userInfo, String role) {
        var pass = new StringBuilder().append(userInfo.getPassword());
        UserCredential userCredential = mapper.toUserCredential(userInfo);
        userCredential.setPasswordHash(passwordEncoder.encode(pass));
        PasswordUtil.clearPassword(userInfo, pass);

        UUID userId = userInfo.getUserId();
        if (userId != null) {
            userCredential.setPasswordActive(false);
            if (repository.existsById(userId)) {
                UserCredential credentialFound = repository.findById(userId).orElseThrow();
                credentialFound.setPasswordActive(userCredential.getPasswordActive());
                credentialFound.setPasswordHash(userCredential.getPasswordHash());
                repository.save(credentialFound);
            } else {
                repository.insertUser(userCredential);
            }
        } else {
            userCredential = repository.save(userCredential);
            userId = userCredential.getUserId();
        }

        try {
            userRoleService.addUserRole(userId, role);
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
        }

        if (userInfo.getUserId() == null) {
            userInfo.setUserId(userId);
            UserRegisteredEvent event = userEventMapper.toUserRegisteredEvent(userInfo);
            userRegistrationProducer.send(event);
        }

        return userCredential;
    }

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
    public void updatePassword(UUID userId, char[] password) {
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
