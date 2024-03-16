package com.ecommerce.app.ecommercebackend.service;


import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;
    @Test
    public void GivenLocalUser_WhenGenerateJWT_ThenReturnJWT(){

        LocalUser user = LocalUser.builder()
                .password("password")
                .email("email@gmail.com")
                .username("username")
                .roles(new ArrayList<>(List.of(new Role("ROLE_USER"))))
                .build();

        Assertions.assertNotNull(jwtService.generateJWT(user));
    }


    @Test
    public void GivenLocalUser_WhenGenerateVerificationJWT_ThenReturnJWT(){
        LocalUser user = LocalUser.builder()
                .password("password")
                .email("email@gmail.com")
                .username("username").build();

        Assertions.assertNotNull(jwtService.generateVerificationJWT(user));
    }


    @Test
    public void GivenJWT_WhenGetUsername_ThenReturnUsername(){
        LocalUser user = LocalUser.builder()
                .password("password")
                .email("email@gmail.com")
                .roles(new ArrayList<>(List.of(new Role("ROLE_USER"))))
                .username("username").build();

        String jwt = jwtService.generateJWT(user);
        String username = user.getUsername();

        Assertions.assertEquals(username, jwtService.getUsername(jwt));
    }
}
