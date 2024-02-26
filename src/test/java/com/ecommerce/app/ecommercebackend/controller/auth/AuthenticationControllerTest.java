package com.ecommerce.app.ecommercebackend.controller.auth;

import com.ecommerce.app.ecommercebackend.api.controller.auth.AuthenticationController;
import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.VerificationToken;
import com.ecommerce.app.ecommercebackend.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
//@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @RegisterExtension
    private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
            .withPerMethodLifecycle(true);

    /*
        REGISTRATION ENDPOINT
     */

    @Test
    public void WhenPostMappingRegisterUser_ThenExpect400() throws Exception {

        RegistrationBody body = RegistrationBody.builder()
                .password("Password123")
                .email("validEmail@gmail.com")
                .username(null)
                .firstName("validFirstName")
                .lastName("validLastName")
                .build();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void WhenPostMappingRegisterUser_ThenExpect2xx() throws Exception {

        RegistrationBody body = RegistrationBody.builder()
                .password("Password125")
                .email("AnotherValidEmailToTest@gmail.com")
                .username("validUsername")
                .firstName("validFirstName")
                .lastName("validLastName")
                .build();

        LocalUser user = LocalUser.builder()
                .username(body.getUsername())
                .firstName(body.getFirstName())
                .email(body.getEmail())
                .lastName(body.getLastName())
                .password(body.getPassword())
                .build();

       Mockito.when(userService.registerUser(body)).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

    }

    /*
        LOGIN ENDPOINT
     */

    @Test
    public void WhenPostMappingLoginUser_ThenExpect200() throws Exception {

        LoginBody loginBody = LoginBody.builder().
                username("UserWantsToLogin").password("Ffhasbcd1234").build();

        Mockito.when(userService.loginUser(any(LoginBody.class))).thenReturn("token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginBody)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", CoreMatchers.is(true)))
                .andExpect(status().isOk());

        Mockito.verify(userService, times(1)).loginUser(any(LoginBody.class));
    }

    @Test
    public void WhenPostMappingLoginUser_ThenExpect400() throws Exception {

        LoginBody loginBody = LoginBody.builder().
                username("UserWantsToLogin").password("Ffhasbcd1234").build();

        Mockito.when(userService.loginUser(any(LoginBody.class))).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginBody)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void WhenPostMappingLoginUser_ThenExpect403Resent() throws Exception {

        LoginBody body = LoginBody.builder().username("UserWantsToLogin").password("Ffhasbcd1234").build();

        Mockito.when(userService.loginUser(any(LoginBody.class))).thenThrow(new UserNotVerifiedException(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", CoreMatchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureReason").value("USER_NOT_VERIFIED_EMAIL_RESENT"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void WhenPostMappingLoginUser_ThenExpect403NotResent() throws Exception {

        LoginBody body = LoginBody.builder().username("UserWantsToLogin").password("Ffhasbcd1234").build();

        Mockito.when(userService.loginUser(any(LoginBody.class))).thenThrow(new UserNotVerifiedException(false));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", CoreMatchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.failureReason").value("USER_NOT_VERIFIED"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    /*
        VERIFY ENDPOINT
     */

    @Test
    @WithMockUser
    public void WhenPostMappingVerifyUser_ThenExpect200() throws Exception {

        Mockito.when(userService.verifyUser(anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("token", "token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void WhenPostingVerifyUser_ThenExpect409() throws Exception {

        Mockito.when(userService.verifyUser(anyString())).thenReturn(false);

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("token", "token"))
                .andExpect(status().is(HttpStatus.CONFLICT.value()));
    }

    /*
        USER PROFILE ENDPOINT
     */

    @Test
    @WithMockUser
    public void WhenGetMappingGetLoggedInUserProfile_ThenExpect200() throws Exception {

        LocalUser user = LocalUser.builder()
                .username("username")
                .firstName("firstname")
                .lastName("lastname")
                .email("email")
                .password("password")
                .build();

        mockMvc.perform(get("/auth/me"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
