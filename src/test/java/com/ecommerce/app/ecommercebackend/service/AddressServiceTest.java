package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.AddressBody;
import com.ecommerce.app.ecommercebackend.api.repository.AddressRepository;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
    @Mock
    private AddressRepository addressRepository;
    @InjectMocks
    private AddressService addressService;

    @Test
    public void GivenAddressBody_WhenAddAddress_ThenReturnAddress(){

        AddressBody addressBody = AddressBody.builder()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .city("city")
                .country("country")
                .build();

        Mockito.when(addressRepository.save(Mockito.any(Address.class))).thenReturn(new Address());

        Assertions.assertNotNull(addressService.addAddress(new LocalUser(),addressBody));
    }
}
