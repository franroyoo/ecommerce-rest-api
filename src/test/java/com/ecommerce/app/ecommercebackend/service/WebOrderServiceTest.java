package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.api.repository.InventoryRepository;
import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.exception.OutOfStockException;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class WebOrderServiceTest {

    @Mock
    private WebOrderRepository webOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private WebOrderService webOrderService;

    // CASE 1: Create order successfully
    // CASE 2: Create order failed (Out of stock)
    // CASE 3: Create order failed (Product does not exist)

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenCreateOrderSuccessfully() throws OutOfStockException, ProductDoesNotExistException {

        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(1000L);
        Mockito.doNothing().when(inventoryRepository).updateQuantityByProductId(Mockito.anyLong(), Mockito.anyLong());
        Mockito.when(webOrderRepository.save(Mockito.any(WebOrder.class))).thenReturn(new WebOrder());

        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        LocalUser user = LocalUser.builder().addresses(Arrays.asList(new Address())).build();

        Assertions.assertNotNull(webOrderService.createOrder(orderBody, user));
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowOutOfStockException() throws OutOfStockException, ProductDoesNotExistException {
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(0L);

        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        Assertions.assertThrows(OutOfStockException.class, () -> webOrderService.createOrder(orderBody, new LocalUser()));
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowProductDoesNotExistException() throws OutOfStockException, ProductDoesNotExistException {

        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        OrderBody orderBody = OrderBody.builder().products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        Assertions.assertThrows(ProductDoesNotExistException.class, () -> webOrderService.createOrder(orderBody, new LocalUser()));
    }
}
