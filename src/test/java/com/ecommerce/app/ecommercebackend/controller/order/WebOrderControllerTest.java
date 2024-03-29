package com.ecommerce.app.ecommercebackend.controller.order;
import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Role;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.service.WebOrderService;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class WebOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private WebOrderService orderService;

    private UsernamePasswordAuthenticationToken authenticatedUser;

    @BeforeEach
    public void setUp() {
        LocalUser user = LocalUser.builder()
                .username("thisIsAValidUsername")
                .emailVerified(true)
                .roles(new ArrayList<>(List.of(new Role("ROLE_USER"))))
                .build();
        authenticatedUser = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
    @Test
    public void WhenGetMappingOrderList_ThenReturn200() throws Exception {

        List<WebOrder> orderList = Arrays.asList(new WebOrder(), new WebOrder(), new WebOrder());

        Mockito.when(orderService.getOrderList(Mockito.any(LocalUser.class))).thenReturn(orderList);

        mockMvc.perform(get("/order/list").with(authentication(authenticatedUser)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    public void WhenPostMappingCreateOrder_ThenReturn200() throws Exception {

        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenReturn(new byte[100]);


        OrderBody orderBody = OrderBody.builder()
                .addressLine1("address")
                .products(Arrays.asList(new ProductBody(2L, 5), new ProductBody(3L,6)))
                .build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void WhenPostMappingCreateOrder_ThenReturn400DueToNoStock() throws Exception {

        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenThrow(new ApiResponseFailureException(FailureType.OUT_OF_STOCK, "Out of stock"));

        OrderBody orderBody = OrderBody.builder()
                .addressLine1("address")
                .products(Arrays.asList(new ProductBody(2L, 5), new ProductBody(3L,6)))
                .build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void WhenPostMappingCreateOrder_ThenReturn404DueToProductNotFound() throws Exception {
        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenThrow(new ApiResponseFailureException(FailureType.PRODUCT_NOT_FOUND, "message"));

        OrderBody orderBody = OrderBody.builder()
                .addressLine1("address")
                .products(Arrays.asList(new ProductBody(2L, 5), new ProductBody(3L,6)))
                .build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void WhenPostMappingCreateOrder_ThenReturn404DueToAddressNotFound() throws Exception {
        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class)))
                .thenThrow(new ApiResponseFailureException(FailureType.ADDRESS_NOT_FOUND, "message"));

        OrderBody orderBody = OrderBody.builder()
                .addressLine1("address")
                .products(Arrays.asList(new ProductBody(2L, 5), new ProductBody(3L,6)))
                .build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(orderBody)))
                        .andDo(print())
                        .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
