package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class EmailServiceTest {

    @RegisterExtension // necesario para testear emails
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

    @Autowired
    private EmailService emailService;

    @Test
    public void GivenVerificationToken_SendVerificationEmail_Successful() throws EmailFailureException, MessagingException {

        VerificationToken verificationToken = new VerificationToken();
        LocalUser localUser = new LocalUser();
        localUser.setEmail("test@example.com");
        verificationToken.setLocalUser(localUser);
        verificationToken.setToken("testToken");


        emailService.sendVerificationEmail(verificationToken);

        MimeMessage[] receivedMessages = greenMailExtension.getReceivedMessages();

        Assertions.assertEquals(1, receivedMessages.length);
        Assertions.assertEquals("Verify your email to activate your account", receivedMessages[0].getSubject());
    }
}
