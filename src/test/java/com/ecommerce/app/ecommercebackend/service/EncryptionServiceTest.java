package com.ecommerce.app.ecommercebackend.service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EncryptionServiceTest {

    @Autowired
    private EncryptionService encryptionService;
    private String plainTextPassword;
    private String encryptedPassword;

    @BeforeEach
    public void setUp(){
        plainTextPassword = "ThisIsMySuperSecretPasswordForTestingABC123";
        encryptedPassword = encryptionService.encryptPassword(plainTextPassword);
    }

    @Test
    public void GivenPlainTextPassword_WhenEncrypting_ThenReturnEncryptedPassword(){
        Assertions.assertNotEquals(plainTextPassword,encryptedPassword);
    }

    @Test
    public void GivenEncryptedPassword_WhenComparingOriginalPasswordAndEncryptedPassword_ThenReturnTrue(){
        Assertions.assertTrue(encryptionService.verifyPassword(plainTextPassword,encryptedPassword));
    }

    @Test
    public void GivenEncryptedPassword_WhenComparingOriginalPasswordAndEncryptedPassword_ThenReturnFalse(){
        Assertions.assertFalse(encryptionService.verifyPassword("ThisIsAnotherSecretPassword", encryptedPassword));
    }
}
