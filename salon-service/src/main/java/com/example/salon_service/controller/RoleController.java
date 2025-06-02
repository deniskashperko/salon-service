package com.example.salon_service.controller;

import com.example.salon_service.entity.Role;
import com.example.salon_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Получить все роли
    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Получить роль по имени
    @GetMapping("/name/{name}")
    public Role getRoleByName(@PathVariable String name) {
        return roleRepository.findByName(name);
    }

    // Добавить новую роль
    @PostMapping
    public Role addRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }
}
