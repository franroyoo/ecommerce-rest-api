package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.api.repository.AddressRepository;
import com.ecommerce.app.ecommercebackend.api.repository.InventoryRepository;
import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
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
import org.springframework.http.HttpStatus;

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

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private WebOrderService webOrderService;

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenCreateOrderSuccessfully(){

        Mockito.when(addressRepository.findByAddressLine1(Mockito.anyString())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(1000L);
        Mockito.doNothing().when(inventoryRepository).updateQuantityByProductId(Mockito.anyLong(), Mockito.anyLong());
        Mockito.when(webOrderRepository.save(Mockito.any(WebOrder.class))).thenReturn(new WebOrder());

        OrderBody orderBody = OrderBody.builder().address_line_1("asasddasdas").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        LocalUser user = LocalUser.builder().addresses(Arrays.asList(new Address())).build();


        Assertions.assertNotNull(webOrderService.createOrder(orderBody, user));
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToNoStock() throws ApiResponseFailureException {

        Mockito.when(addressRepository.findByAddressLine1(Mockito.anyString())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(0L);

        OrderBody orderBody = OrderBody.builder().address_line_1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, new LocalUser()));
        Assertions.assertEquals(ex.getHttpStatus(), HttpStatus.CONFLICT);
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToProductNotFound() throws ApiResponseFailureException{

        Mockito.when(addressRepository.findByAddressLine1(Mockito.anyString())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        OrderBody orderBody = OrderBody.builder().address_line_1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, new LocalUser()));
        Assertions.assertEquals(ex.getHttpStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToAddressNotFound(){

        Mockito.when(addressRepository.findByAddressLine1(Mockito.anyString())).thenReturn(Optional.empty());

        OrderBody orderBody = OrderBody.builder().address_line_1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, new LocalUser()));
        Assertions.assertEquals(ex.getHttpStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void GivenOrderId_WhenDeleteOrder_ThenDeleteOrderSuccessfully() throws ApiResponseFailureException{
        Mockito.when(webOrderRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new WebOrder()));
        Mockito.doNothing().when(webOrderRepository).deleteById(Mockito.anyLong());

        Assertions.assertDoesNotThrow(() -> webOrderService.deleteOrder(5L));
    }

    @Test
    public void GivenOrderId_WhenDeleteOrder_ThenThrowExceptionDueToNotFound(){
        Mockito.when(webOrderRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.deleteOrder(5L));
        Assertions.assertEquals(ex.getHttpStatus(), HttpStatus.BAD_REQUEST);
    }
}
