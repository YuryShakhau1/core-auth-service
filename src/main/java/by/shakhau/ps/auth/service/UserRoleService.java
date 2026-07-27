package by.shakhau.ps.auth.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRoleService {

    List<String> findUserRoles(UUID userId);
    void addUserRole(UUID userId, Long roleId);
    void addUserRole(UUID userId, String roleNames);
    void addUserRoles(UUID userId, Collection<String> roleNames);
    void deleteUserRole(UUID userId, String roleName);
}
