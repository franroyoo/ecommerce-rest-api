package com.ecommerce.app.ecommercebackend.controller.order;
import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.exception.OutOfStockException;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.service.WebOrderService;
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
        LocalUser user = LocalUser.builder().username("thisIsAValidUsername").emailVerified(true).build();
        authenticatedUser = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
    }
    @Test
    @WithUserDetails
    public void WhenGetMappingOrderList_ThenReturn200() throws Exception {

        List<WebOrder> orderList = Arrays.asList(new WebOrder(), new WebOrder(), new WebOrder());

        Mockito.when(orderService.getOrderList(Mockito.any(LocalUser.class))).thenReturn(orderList);

        mockMvc.perform(get("/order/list").with(authentication(authenticatedUser)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // TODO: Refactor unit testing for order creation (exception handling changed)
    @Test
    @WithUserDetails
    public void WhenPostMappingCreateOrder_ThenReturn200() throws Exception {

        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenReturn(new WebOrder());

        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(), new ProductBody())).build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithUserDetails
    public void WhenPostMappingCreateOrder_ThenReturn400() throws Exception {

        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenThrow(new OutOfStockException());

        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(), new ProductBody())).build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithUserDetails
    public void WhenPostMappingCreateOrder_ThenReturn409() throws Exception {
        Mockito.when(orderService.createOrder(Mockito.any(OrderBody.class), Mockito.any(LocalUser.class))).thenThrow(new ProductDoesNotExistException());

        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(), new ProductBody())).build();

        mockMvc.perform(post("/order/new").with(authentication(authenticatedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderBody)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }
}
