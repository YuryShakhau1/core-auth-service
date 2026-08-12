package by.shakhau.ps.auth.service;

import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.model.UserShortCredential;
import by.shakhau.ps.auth.service.model.UserInfo;

import java.util.UUID;

public interface UserCredentialService {

    UserShortCredential findByEmail(String email);
    UserCredential findByUserId(UUID userId);
    UserCredential registerUser(UserInfo userInfo, String role);
    void registerExternalUser(UserInfo userInfo, String role);
    void update(UserInfo userInfo);
    void updatePassword(UUID userId, StringBuilder password);
    void updateActive(UUID userId, Boolean active);
}
