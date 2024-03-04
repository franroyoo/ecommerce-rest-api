package com.ecommerce.app.ecommercebackend.controller.product;

import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void WhenGetMappingProductList_ThenReturnProductList() throws Exception {

       List<Product> productList = Arrays.asList(new Product(), new Product(), new Product());

       Mockito.when(productService.getProductList()).thenReturn(productList);


        mockMvc.perform(get("/product")).andDo(print()).andExpect(status().is(HttpStatus.OK.value()));

        //Mockito.verify(productService, Mockito.times(1)).getProductList();
    }

    @Test
    public void WhenGetMappingProductById_ThenReturnProduct() throws Exception {

        Product product = new Product();
        Mockito.when(productService.getProductById(Mockito.any(Long.class))).thenReturn(product);

        mockMvc.perform(get("/product/{id}", 1L)).andDo(print()).andExpect(status().is(HttpStatus.OK.value()));
    }
}
