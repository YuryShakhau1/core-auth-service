package by.shakhau.ps.auth.service.impl;

import by.shakhau.ps.auth.repository.RoleRepository;
import by.shakhau.ps.auth.service.RoleService;
import by.shakhau.ps.auth.service.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;

    @Override
    public List<String> findAll() {
        return repository.findAllRoleNames();
    }

    @Override
    public List<String> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Cacheable(value = "roles.id", key = "#name")
    @Override
    public Long findIdByName(String name) {
        return repository.findIdByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role name %s not found".formatted(name)));
    }
}
