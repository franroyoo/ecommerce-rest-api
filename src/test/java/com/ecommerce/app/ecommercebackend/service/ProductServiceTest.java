package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void GivenProduct_WhenGetProductById_ThenReturnProduct(){

        Mockito.when(productRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(new Product()));

        Assertions.assertNotNull(productService.getProductById(Mockito.any(Long.class)));
    }

    @Test
    public void WhenGetProductList_ThenReturnProductList(){
        Mockito.when(productRepository.findAll()).thenReturn(Arrays.asList(new Product()));

        Assertions.assertNotNull(productService.getProductList());
    }

    @Test
    public void GivenProduct_WhenGetProductById_ThenThrowExceptionDueToNotFound(){
        Mockito.when(productRepository.findById(Mockito.any(Long.class))).thenThrow(new ApiResponseFailureException(FailureType.PRODUCT_NOT_FOUND, "extraDetails"));

        Assertions.assertThrows(ApiResponseFailureException.class, () -> productService.getProductById(Mockito.any(Long.class)));
    }

}
