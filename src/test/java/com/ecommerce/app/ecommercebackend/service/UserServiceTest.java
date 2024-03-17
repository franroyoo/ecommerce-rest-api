package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.auth.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.auth.RegistrationBody;
import com.ecommerce.app.ecommercebackend.api.repository.LocalUserRepository;
import com.ecommerce.app.ecommercebackend.api.repository.RoleRepository;
import com.ecommerce.app.ecommercebackend.api.repository.VerificationTokenRepository;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserBadCredentialsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.*;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

    @Mock
    private LocalUserRepository localUserRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private JWTService jwtService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    /*
        REGISTER USER TESTS
     */
    @Test
    public void GivenExistingUser_WhenRegisterUserWithExistingUsername_ThenThrowException() throws EmailFailureException {

        RegistrationBody registrationBody = new RegistrationBody();
        registrationBody.setUsername("Username-Exists");
        registrationBody.setEmail("Email-DoesNot-Exist");


        when(localUserRepository.findByUsernameIgnoreCase(registrationBody.getUsername())).thenReturn(Optional.of(new LocalUser()));


        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registrationBody));

        verify(localUserRepository, times(1)).findByUsernameIgnoreCase(registrationBody.getUsername());
        verify(localUserRepository, never()).findByEmailIgnoreCase(anyString());
        verify(encryptionService, never()).encryptPassword(anyString());
        verify(localUserRepository, never()).save(any(LocalUser.class));
        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(emailService, never()).sendVerificationEmail(any(VerificationToken.class));
    }

    @Test
    public void GivenExistingUser_WhenRegisterUserWithExistingEmail_ThenThrowException() throws EmailFailureException {

        RegistrationBody registrationBody = new RegistrationBody();
        registrationBody.setEmail("Email-Exists");
        registrationBody.setUsername("Username-DoesNot-Exist");

        when(localUserRepository.findByEmailIgnoreCase(registrationBody.getEmail())).thenReturn(Optional.of(new LocalUser()));
        when(localUserRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());



        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registrationBody));

        // Verificar interacciones
        verify(localUserRepository, times(1)).findByEmailIgnoreCase(registrationBody.getEmail());
        verify(localUserRepository, times(1)).findByUsernameIgnoreCase(anyString());
        verify(encryptionService, never()).encryptPassword(anyString());
        verify(localUserRepository, never()).save(any(LocalUser.class));
        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(emailService, never()).sendVerificationEmail(any(VerificationToken.class));
    }

    @Test
    public void GivenUser_WhenRegisterUser_ThenReturnUser() throws EmailFailureException, UserAlreadyExistsException {

        RegistrationBody registrationBody = RegistrationBody.builder()
                .username("username").password("password").email("email").firstName("firstname").lastName("lastname")
                .build();

        when(localUserRepository.findByUsernameIgnoreCase(registrationBody.getUsername())).thenReturn(Optional.empty());
        when(localUserRepository.findByEmailIgnoreCase(registrationBody.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(new Role("ROLE_USER"));

        LocalUser user = LocalUser.builder()
                .username(registrationBody.getUsername())
                .email(registrationBody.getEmail())
                .firstName(registrationBody.getFirstName())
                .roles(Collections.singletonList(new Role("ROLE_USER")))
                .lastName(registrationBody.getLastName())
                .password("encryptedPass")
                .build();

        when(encryptionService.encryptPassword(anyString())).thenReturn("encryptedPass");

        when(localUserRepository.save(any(LocalUser.class))).thenReturn(user);

        when(jwtService.generateVerificationJWT(any(LocalUser.class))).thenReturn("superSecretGeneratedJWT");

        doNothing().when(emailService).sendVerificationEmail(any(VerificationToken.class));

        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(new VerificationToken());

        LocalUser savedUser = userService.registerUser(registrationBody);

        Assertions.assertNotNull(savedUser);

        verify(jwtService).generateVerificationJWT(any(LocalUser.class));
        verify(emailService).sendVerificationEmail(any(VerificationToken.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    /*
        LOGIN USER TESTS
     */

    @Test
    public void GivenUser_WhenLoginUser_ThenThrowUserBadCredentialsException() throws UserNotVerifiedException, EmailFailureException {
        LoginBody loginBody = LoginBody.builder().username("Non-Existent-User").password("Random-Password").build();
        when(localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername())).thenReturn(Optional.empty());

        Assertions.assertThrows(UserBadCredentialsException.class, () -> userService.loginUser(loginBody));
    }

    @Test
    public void GivenUser_WhenLoginUser_ThenReturnJWT() throws UserNotVerifiedException, EmailFailureException {
        LoginBody loginBody = LoginBody.builder().username("username").password("password").build();

        LocalUser localUser = LocalUser.builder()
                .username(loginBody.getUsername())
                .firstName("firstname")
                .lastName("lastname")
                .password("encryptedPasswordInDatabase")
                .emailVerified(true).build();

        when(localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername())).thenReturn(Optional.of(localUser));
    
        when(encryptionService.verifyPassword(loginBody.getPassword(),localUser.getPassword())).thenReturn(true);
        when(jwtService.generateJWT(localUser)).thenReturn("HereIsYourToken");

        Assertions.assertNotNull(userService.loginUser(loginBody));
    }

    @Test
    public void GivenUser_WhenLoginUser_ThenResendEmailThrowException() throws EmailFailureException {
        LoginBody loginBody = LoginBody.builder().username("username").password("password").build();

        LocalUser localUser = LocalUser.builder()
                .username(loginBody.getUsername())
                .firstName("firstname")
                .lastName("lastname")
                .password("encryptedPasswordInDatabase")
                .emailVerified(false)
                .verificationTokens(new ArrayList<>())
                .build();

        when(localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername())).thenReturn(Optional.of(localUser));

        when(encryptionService.verifyPassword(loginBody.getPassword(),localUser.getPassword())).thenReturn(true);

        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(new VerificationToken());

        doNothing().when(emailService).sendVerificationEmail(any(VerificationToken.class));

        Assertions.assertThrows(UserNotVerifiedException.class, () -> userService.loginUser(loginBody));
    }

    @Test
    public void GivenUser_WhenLoginUser_ThenNotResendEmailThrowException() throws EmailFailureException {
        LoginBody loginBody = LoginBody.builder().username("username").password("password").build();

        LocalUser localUser = LocalUser.builder()
                .username(loginBody.getUsername())
                .firstName("firstname")
                .lastName("lastname")
                .password("encryptedPasswordInDatabase")
                .emailVerified(false)
                .verificationTokens(new ArrayList<>())
                .build();

        // Con tal de que la lista tenga un solo verification token creado recientemente ya basta para q resend sea falso

        localUser.getVerificationTokens().add(VerificationToken.builder().token("aSfKgsLdDGaSFf").createdTimestamp(new Timestamp(System.currentTimeMillis())).build());

        when(localUserRepository.findByUsernameIgnoreCase(loginBody.getUsername())).thenReturn(Optional.of(localUser));

        when(encryptionService.verifyPassword(loginBody.getPassword(),localUser.getPassword())).thenReturn(true);

        Assertions.assertThrows(UserNotVerifiedException.class, () -> userService.loginUser(loginBody));

        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
        verify(emailService, never()).sendVerificationEmail(any(VerificationToken.class));
    }

    /*
        USER VERIFICATION TESTS
     */

    @Test
    public void GivenVerificationToken_WhenVerifyUserIncorrectToken_ThenReturnFalse(){
        String token = "ASDASD";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        Assertions.assertFalse(userService.verifyUser(token));

        verify(verificationTokenRepository,times(1)).findByToken(token);
    }

    @Test
    public void GivenVerificationToken_WhenVerifyUser_ThenReturnTrue(){

        LocalUser localUser = LocalUser.builder().emailVerified(false).build();

        VerificationToken verificationToken = VerificationToken.builder().localUser(localUser).build();

        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));
        when(localUserRepository.save(any(LocalUser.class))).thenReturn(localUser);
        doNothing().when(verificationTokenRepository).deleteByLocalUser(localUser);

        Assertions.assertTrue(userService.verifyUser(anyString()));
        verify(verificationTokenRepository, times(1)).findByToken(anyString());
        verify(localUserRepository,times(1)).save(localUser);
        verify(verificationTokenRepository,times(1)).deleteByLocalUser(localUser);
    }

    @Test
    public void GivenVerificationToken_WhenVerifyUserWithVerifiedEmail_ThenReturnFalse(){
        LocalUser localUser = LocalUser.builder().emailVerified(true).build();
        VerificationToken verificationToken = VerificationToken.builder().localUser(localUser).build();

        when(verificationTokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        Assertions.assertFalse(userService.verifyUser(anyString()));

        verify(verificationTokenRepository,times(1)).findByToken(anyString());
    }

}
