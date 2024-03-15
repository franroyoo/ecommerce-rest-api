package com.ecommerce.app.ecommercebackend.config;
import com.ecommerce.app.ecommercebackend.api.repository.RoleRepository;
import com.ecommerce.app.ecommercebackend.model.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_PREMIUM");
        createRoleIfNotExists("ROLE_ADMIN");
    }

    private void createRoleIfNotExists(String roleName) {

        Optional<Role> opRole = roleRepository.findByName(roleName);

        if (opRole.isEmpty()){
            roleRepository.save(Role.builder().name(roleName).build());
        }
    }
}
