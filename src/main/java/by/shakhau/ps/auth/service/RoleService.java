package by.shakhau.ps.auth.service;

import by.shakhau.ps.auth.model.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<String> findAllRoleNames();
    List<Role> findAll();
    List<String> findNamesByUserId(UUID userId);
    List<Role> findByUserId(UUID userId);
    Long findIdByName(String name);
}
