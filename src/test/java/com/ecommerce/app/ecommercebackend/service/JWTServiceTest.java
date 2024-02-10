package com.ecommerce.app.ecommercebackend.service;


import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private LocalUserRepository localUserRepository;

    @Test
    public void GivenLocalUser_WhenGenerateJWT_ThenReturnJWT(){
        LocalUser user = localUserRepository.findByEmailIgnoreCase("UserA@junit.com").get();

        Assertions.assertNotNull(jwtService.generateJWT(user));
    }

    @Test
    public void GivenLocalUser_WhenGenerateVerificationJWT_ThenReturnJWT(){
        LocalUser user = localUserRepository.findByEmailIgnoreCase("UserA@junit.com").get();

        Assertions.assertNotNull(jwtService.generateVerificationJWT(user));
    }

    @Test
    public void GivenJWT_WhenGetUsername_ThenReturnUsername(){
        LocalUser user = localUserRepository.findByEmailIgnoreCase("UserA@junit.com").get();

        Assertions.assertNotNull(jwtService.generateJWT(user));

        String jwt = jwtService.generateJWT(user);
        String username = user.getUsername();

        Assertions.assertEquals(username, jwtService.getUsername(jwt));
    }
}
