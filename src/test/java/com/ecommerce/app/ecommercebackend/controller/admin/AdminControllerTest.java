package com.ecommerce.app.ecommercebackend.controller.admin;

import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.service.AdminService;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockmvc;

    @MockBean
    private AdminService adminService;

    private UsernamePasswordAuthenticationToken authenticatedAdmin;

    @BeforeEach
    public void init(){
        LocalUser user = LocalUser.builder()
                .username("thisIsAValidUsername")
                .emailVerified(true)
                .roles(new ArrayList<>(List.of(new Role("ROLE_ADMIN"))))
                .build();

        authenticatedAdmin = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    public void WhenPutMappingPromoteUserToPremium_ThenReturn200() throws Exception {

        LocalUser userWithPremium = LocalUser.builder().roles(new ArrayList<>(List.of(new Role("ROLE_PREMIUM")))).build();

        Mockito.when(adminService.promoteUserToPremium(Mockito.anyLong())).thenReturn(userWithPremium);

        mockmvc.perform(put("/admin/{id}", 1L).with(authentication(authenticatedAdmin)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void WhenPutMappingPromoteUserToPremium_ThenReturn404() throws Exception {

        Mockito.when(adminService.promoteUserToPremium(Mockito.anyLong()))
                .thenThrow(new ApiResponseFailureException(FailureType.PRODUCT_NOT_FOUND, "detail"));

        mockmvc.perform(put("/admin/{id}", 1L).with(authentication(authenticatedAdmin)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void WhenPutMappingPromoteUserToPremium_ThenReturn400() throws Exception {

        Mockito.when(adminService.promoteUserToPremium(Mockito.anyLong()))
                .thenThrow(new ApiResponseFailureException(FailureType.USER_AUTHORITY_EXISTS, "detail"));

        mockmvc.perform(put("/admin/{id}", 1L).with(authentication(authenticatedAdmin)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
