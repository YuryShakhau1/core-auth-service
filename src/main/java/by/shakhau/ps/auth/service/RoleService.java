package by.shakhau.ps.auth.service;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<String> findAll();
    List<String> findByUserId(UUID userId);
    Long findIdByName(String name);
}
