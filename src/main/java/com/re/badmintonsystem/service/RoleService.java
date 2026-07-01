package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.RoleRequest;
import com.re.badmintonsystem.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> findAll();

    Role findById(Long id);

    Role create(RoleRequest request);

    Role update(Long id, RoleRequest request);

    void delete(Long id);
}
