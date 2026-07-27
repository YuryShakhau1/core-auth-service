package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.service.RoleService;
import by.shakhau.ps.auth.service.UserRoleService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleService roleService;
    private final UserCredentialRepository repository;

    @Override
    public List<String> findUserRoles(UUID userId) {
        return roleService.findByUserId(userId);
    }

    @Transactional
    @Override
    public void addUserRole(UUID userId, Long roleId) {
        repository.addUserRole(userId, roleId);
    }

    @Override
    public void addUserRole(UUID userId, String roleNames) {
        repository.addUserRole(userId, roleService.findIdByName(roleNames));
    }

    @Transactional
    @Override
    public void addUserRoles(UUID userId, Collection<String> roleNames) {
        for (String roleName: roleNames) {
            addUserRole(userId, roleName);
        }
    }

    @Override
    public void deleteUserRole(UUID userId, String roleName) {
        repository.deleteUserRole(userId, roleService.findIdByName(roleName));
    }
}
