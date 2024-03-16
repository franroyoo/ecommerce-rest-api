package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.RoleRepository;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private LocalUserRepository localUserRepository;
    private RoleRepository roleRepository;

    @Autowired
    public AdminService(LocalUserRepository localUserRepository, RoleRepository roleRepository) {
        this.localUserRepository = localUserRepository;
        this.roleRepository = roleRepository;
    }

    public LocalUser promoteUserToPremium(Long userId){

        LocalUser user = localUserRepository.findById(userId)
                .orElseThrow(() -> new ApiResponseFailureException(FailureType.USER_NOT_FOUND, "User not found, make sure you introduced the correct ID"));


        if (!user.isPremium()){

            Role rolePremium = roleRepository.findByName("ROLE_PREMIUM");

            user.getRoles().add(rolePremium);
            localUserRepository.save(user);

            return user;
        } else throw new ApiResponseFailureException(FailureType.USER_AUTHORITY_EXISTS, "The user has premium authority already");
    }
}
