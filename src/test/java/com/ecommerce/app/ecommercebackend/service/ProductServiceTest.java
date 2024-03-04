package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

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
        // Arrange, act, assert

        Mockito.when(productRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(new Product()));

        Assertions.assertNotNull(productService.getProductById(Mockito.any(Long.class)));
    }

    @Test
    public void GivenProduct_WhenGetProductById_ThenThrowProductDoesNotExistException(){
        Mockito.when(productRepository.findById(Mockito.any(Long.class))).thenThrow(new ProductDoesNotExistException());

        Assertions.assertThrows(ProductDoesNotExistException.class, () -> productService.getProductById(Mockito.any(Long.class)));
    }
}
