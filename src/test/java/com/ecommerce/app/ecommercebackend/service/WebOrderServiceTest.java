package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.api.dto.ProductBody;
import com.ecommerce.app.ecommercebackend.api.dto.invoice.InvoiceJsonBody;
import com.ecommerce.app.ecommercebackend.api.repository.AddressRepository;
import com.ecommerce.app.ecommercebackend.api.repository.InventoryRepository;
import com.ecommerce.app.ecommercebackend.api.repository.ProductRepository;
import com.ecommerce.app.ecommercebackend.api.repository.WebOrderRepository;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.Product;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.validation.FailureType;
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

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private WebOrderService webOrderService;

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenCreateOrderSuccessfullyAndReturnInvoice(){

        Mockito.when(addressRepository.findByAddressLine1AndLocalUser_Id(Mockito.any(String.class), Mockito.anyInt())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(1000L);
        Mockito.doNothing().when(inventoryRepository).updateQuantityByProductId(Mockito.anyLong(), Mockito.anyLong());
        Mockito.when(webOrderRepository.save(Mockito.any(WebOrder.class))).thenReturn(new WebOrder());

        OrderBody orderBody = OrderBody.builder().addressLine1("asasddasdas").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        LocalUser user = LocalUser.builder().id(1).build();

        byte[] bytes = new byte[10];

        Mockito.when(invoiceService.generateInvoice(Mockito.any(InvoiceJsonBody.class))).thenReturn(bytes);

        Assertions.assertNotNull(webOrderService.createOrder(orderBody, user));
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToNoStock() throws ApiResponseFailureException {

        Mockito.when(addressRepository.findByAddressLine1AndLocalUser_Id(Mockito.any(String.class), Mockito.anyInt())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Product.builder().id(1L).build()));
        Mockito.when(inventoryRepository.findQuantityByProductId(Mockito.anyLong())).thenReturn(0L);

        OrderBody orderBody = OrderBody.builder().addressLine1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, LocalUser.builder().id(1).build()));
        Assertions.assertEquals(ex.getFailureType(), FailureType.OUT_OF_STOCK);
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToProductNotFound() throws ApiResponseFailureException{

        Mockito.when(addressRepository.findByAddressLine1AndLocalUser_Id(Mockito.any(String.class), Mockito.anyInt())).thenReturn(Optional.of(new Address()));
        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        OrderBody orderBody = OrderBody.builder().addressLine1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, LocalUser.builder().id(1).build()));
        Assertions.assertEquals(ex.getFailureType(), FailureType.PRODUCT_NOT_FOUND);
    }

    @Test
    public void GivenOrderBody_WhenCreateOrder_ThenThrowExceptionDueToAddressNotFound(){

        Mockito.when(addressRepository.findByAddressLine1AndLocalUser_Id(Mockito.any(String.class), Mockito.anyInt())).thenReturn(Optional.empty());

        OrderBody orderBody = OrderBody.builder().addressLine1("addressLine1").products(Arrays.asList(new ProductBody(1L, 1), new ProductBody(1L, 1))).build();

        ApiResponseFailureException ex = Assertions.assertThrows(ApiResponseFailureException.class, () -> webOrderService.createOrder(orderBody, LocalUser.builder().id(1).build()));
        Assertions.assertEquals(ex.getFailureType(), FailureType.ADDRESS_NOT_FOUND);
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
        Assertions.assertEquals(ex.getFailureType(), FailureType.ORDER_NOT_FOUND);
    }
}
