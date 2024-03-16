package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.RoleRepository;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @Mock
    private LocalUserRepository localUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AdminService adminService;

    @Test
    public void GivenUserId_WhenPromoteUserToPremium_ThenPromoteUserToPremium(){

        LocalUser userWithoutPremium = LocalUser.builder().roles(new ArrayList<>(List.of(new Role("ROLE_USER")))).build();

        Mockito.when(localUserRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(userWithoutPremium));

        Mockito.when(roleRepository.findByName("ROLE_PREMIUM")).thenReturn(new Role("ROLE_PREMIUM"));

        Mockito.when(localUserRepository.save(Mockito.any(LocalUser.class))).thenReturn(new LocalUser());

        Assertions.assertNotNull(adminService.promoteUserToPremium(1L));
    }

    @Test
    public void GivenUserId_WhenPromoteUserToPremium_ThenThrowExceptionDueToUserNotFound(){
        Mockito.when(localUserRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

       ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> adminService.promoteUserToPremium(1L));
       Assertions.assertEquals(ex.getFailureType(), FailureType.USER_NOT_FOUND);
    }

    @Test
    public void GivenUserId_WhenPromoteUserToPremium_ThenThrowExceptionDueToUserAlreadyHasPremium(){

        LocalUser userWithPremium = LocalUser.builder().roles(new ArrayList<>(List.of(new Role("ROLE_PREMIUM")))).build();

        Mockito.when(localUserRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(userWithPremium));

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> adminService.promoteUserToPremium(1L));
        Assertions.assertEquals(ex.getFailureType(), FailureType.USER_AUTHORITY_EXISTS);
    }
}
