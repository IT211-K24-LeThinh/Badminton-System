package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.RoleRequest;
import com.re.badmintonsystem.entity.Role;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ConflictException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.RoleRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    @Transactional
    public Role create(RoleRequest request) {
        if (roleRepository.existsByName(request.getName().toUpperCase())) {
            throw new ConflictException("Role '" + request.getName() + "' already exists");
        }

        Role role = new Role();
        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());

        role = roleRepository.save(role);
        log.info("Role created: {}", role.getName());
        return role;
    }

    @Override
    @Transactional
    public Role update(Long id, RoleRequest request) {
        Role role = findById(id);

        // Prevent renaming built-in roles
        if (isBuiltInRole(role.getName())) {
            throw new BadRequestException("Cannot modify built-in role: " + role.getName());
        }

        if (!role.getName().equalsIgnoreCase(request.getName())
                && roleRepository.existsByName(request.getName().toUpperCase())) {
            throw new ConflictException("Role '" + request.getName() + "' already exists");
        }

        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());

        role = roleRepository.save(role);
        log.info("Role updated: {}", role.getName());
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = findById(id);

        if (isBuiltInRole(role.getName())) {
            throw new BadRequestException("Cannot delete built-in role: " + role.getName());
        }

        // Check if any users still have this role
        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getId().equals(id)))
                .count();

        if (userCount > 0) {
            throw new BadRequestException(
                    "Cannot delete role '" + role.getName() + "': " + userCount + " user(s) still have this role");
        }

        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }

    private boolean isBuiltInRole(String roleName) {
        return "ADMIN".equalsIgnoreCase(roleName)
                || "MANAGER".equalsIgnoreCase(roleName)
                || "CUSTOMER".equalsIgnoreCase(roleName);
    }
}
