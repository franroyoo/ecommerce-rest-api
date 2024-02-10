package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.VerificationTokenRepository;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @RegisterExtension // necesario para testear emails
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

//    @Mock
//    private LocalUserRepository localUserRepository; // inyeccion de dependencia del repositorio
//    @Mock
//    private VerificationTokenRepository verificationTokenRepository;
//    @Mock
//    private EncryptionService encryptionService;
//    @Mock
//    private JWTService jwtService;
//    @Mock
//    private EmailService emailService;
//
//    @InjectMocks
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private UserService userService;

    @Test
    @Transactional // testea lo de sql y luego hace rollback para seguir testeando otros casos
    public void UserService_RegisterUser_ReturnUserWithVerificationEmailSent() throws MessagingException {

        // Arrange
        RegistrationBody body = RegistrationBody.builder().firstName("FirstName")
                .lastName("LastName")
                .email("UserServiceTest$testRegisterUser@junit.com")
                .password("MySecretPassword123")
                .username("UserA")
                .build();

        // Tendria que tirar error porque UserA ya existe en la base de datos

        // Act and assert at the same time
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body), "Username should already be in use"); // asegurar de q throwea

        body.setUsername("UserServiceTest$testRegisterUser");
        body.setEmail("UserA@junit.com");

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body), "Email should already be in use"); // asegurar de q throwea

        // Assert user does not exist, thus successful registration

        body.setEmail("UserServiceTest$testRegisterUser@junit.com");
        Assertions.assertDoesNotThrow(() -> userService.registerUser(body)); // asegurar de que NO throwea

        // Nos aseguramos que el email fue enviado correctamente
        Assertions.assertEquals(body.getEmail(), greenMailExtension.getReceivedMessages()[0]
                .getRecipients(Message.RecipientType.TO)[0].toString());

    }

    @Test
    @Transactional
    public void UserService_LoginUser_ReturnJWT() throws UserNotVerifiedException, EmailFailureException {

        // Assert user no existe
        LoginBody loginBody = new LoginBody();
        loginBody.setUsername("UserA-NotExists");
        loginBody.setPassword("PasswordA123");

        Assertions.assertNull(userService.loginUser(loginBody), "User could not be verified, NULL");

        // Assert incorrect password
        loginBody.setUsername("UserA");
        loginBody.setPassword("Password-DoesNot-Exist");

        Assertions.assertNull(userService.loginUser(loginBody));

        // Assert throws UserNotVerifiedException with resend value being true
        loginBody.setUsername("UserB");
        loginBody.setPassword("PasswordB123");

        try{ // uso try catch para acceder al valor de isNewEmailSent
            userService.loginUser(loginBody);
            Assertions.fail("User should not have email verified");
        }catch (UserNotVerifiedException ex){
            Assertions.assertTrue(ex.isNewEmailSent(), "Email should be sent");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length); // debería haber un mensaje
        }


        // Assert throws UserNotVerifiedExceptio with resend value being false
        try{ // uso try catch para acceder al valor de isNewEmailSent
            userService.loginUser(loginBody);
            Assertions.fail("User should not have email verified");
        }catch (UserNotVerifiedException ex){
            Assertions.assertFalse(ex.isNewEmailSent(), "Email should be sent");
            Assertions.assertEquals(1,greenMailExtension.getReceivedMessages().length); // debería estar vacio
        }

        //Assertions.assertThrows(UserNotVerifiedException.class, () -> userService.loginUser(loginBody));

        // Assert return JWT
        loginBody.setUsername("UserA");
        loginBody.setPassword("PasswordA123");

        Assertions.assertNotNull(userService.loginUser(loginBody));

    }


    @Test
    @Transactional
    public void UserService_VerifyUser_UpdateIsEmailVerifiedAndReturnTrue(){

        // Assert token falso return false
        Assertions.assertFalse(userService.verifyUser("Fake-Token"));

        // Assert token verdadero con email sin verificar return true
        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserB");
        loginBody.setPassword("PasswordB123");

        try {
            userService.loginUser(loginBody);
            Assertions.fail();
        } catch (UserNotVerifiedException e) {

            List<VerificationToken> tokens = verificationTokenRepository.findByLocalUser_IdOrderByIdDesc(2);
            String token = tokens.get(0).getToken();

            Assertions.assertTrue(userService.verifyUser(token));
            Assertions.assertNotNull(loginBody); // asegurándonos que el user se puede loguear
        } catch (EmailFailureException e) {
            throw new RuntimeException(e);
        }
    }
}
