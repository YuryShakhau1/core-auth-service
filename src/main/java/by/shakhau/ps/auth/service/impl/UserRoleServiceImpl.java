package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.model.Role;
import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.service.RoleService;
import by.shakhau.ps.auth.service.UserRoleService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserCredentialRepository repository;
    private final RoleService roleService;

    @Override
    public List<String> findUserRoleNames(UUID userId) {
        return roleService.findNamesByUserId(userId);
    }

    @Transactional
    @Override
    public void addUserRole(UUID userId, Long roleId) {
        repository.addUserRole(userId, roleId);
    }

    @Transactional
    @Override
    public void addUserRole(UUID userId, String roleName) {
        repository.addUserRole(userId, roleService.findIdByName(roleName));
    }

    @Transactional
    @Override
    public void updateUserRoles(UUID userId, Collection<String> roleNames) {
        Set<String> roleSet = new HashSet<>(roleNames);
        List<Role> userRoles = roleService.findByUserId(userId);
        Set<String> userRoleNames = userRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        List<Long> rolesToDelete = userRoles.stream()
                .filter(ur -> !roleSet.contains(ur.getName()))
                .map(Role::getId)
                .toList();
        List<Role> rolesToAdd = roleService.findAll().stream()
                .filter(ur -> !userRoleNames.contains(ur.getName()))
                .filter(r -> roleSet.contains(r.getName()))
                .toList();
        repository.deleteUserRoles(userId, rolesToDelete);
        for (Role role : rolesToAdd) {
            repository.addUserRole(userId, role.getId());
        }
    }

    @Transactional
    @Override
    public void deleteUserRole(UUID userId, String roleName) {
        repository.deleteUserRole(userId, roleService.findIdByName(roleName));
    }
}
