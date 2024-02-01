package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

    @RegisterExtension // necesario para testear emails
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);


    @Autowired
    private UserService userService;

    @Test
    @Transactional // testea lo de sql y luego hace rollback para seguir testeando otros casos
    public void testRegisterUser() throws MessagingException {

        RegistrationBody body = new RegistrationBody();

        body.setFirstName("FirstName");
        body.setLastName("LastName");
        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        body.setPassword("MySecretPassword123");
        body.setUsername("UserA");

        // Tendria que tirar error porque UserA ya existe en la base de datos

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body), "Username should already be in use"); // asegurar de q throwea

        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body), "Email should already be in use"); // asegurar de q throwea

        Assertions.assertDoesNotThrow(() -> userService.registerUser(body), "User should register succesfully"); // asegurar de que NO throwea

        // Nos aseguramos que el email fue enviado correctamente
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());

    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {

        // test find user

        LoginBody loginBody = new LoginBody();

        // Assert user inexistente (null)
        loginBody.setUsername("UserC");
        loginBody.setPassword("blablablabla");
        Assertions.assertNull(userService.loginUser(loginBody), "User could not be verified, NULL");

        // Assert user no esta verificado


    }

}
