package com.ecommerce.app.ecommercebackend.controller.address;

import com.ecommerce.app.ecommercebackend.api.dto.AddressBody;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @MockBean
    private AddressService addressService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken authenticatedUser;

    @BeforeEach
    public void init(){

        LocalUser user = LocalUser.builder()
                .username("thisIsAValidUsername")
                .emailVerified(true)
                .roles(new ArrayList<>(List.of(new Role("ROLE_USER"))))
                .build();
        authenticatedUser = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    public void WhenGetMappingAddressList_ThenReturnAddressList() throws Exception {

        mockMvc.perform(get("/addresses").with(authentication(authenticatedUser)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void WhenPostMappingAddress_ThenReturnAddress() throws Exception {

        Mockito.when(addressService.addAddress(Mockito.any(LocalUser.class), Mockito.any(AddressBody.class))).thenReturn(new Address());

        AddressBody addressBody = AddressBody.builder()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .city("city")
                        .country("country")
                        .build();

        mockMvc.perform(post(
                "/addresses/new").with(authentication(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
