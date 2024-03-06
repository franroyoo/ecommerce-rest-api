package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.AddressBody;
import com.ecommerce.app.ecommercebackend.api.repository.AddressRepository;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    private AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address addAddress(LocalUser localUser, AddressBody addressBody){
        Address address = Address.builder()
                .addressLine1(addressBody.getAddressLine1())
                .addressLine2(addressBody.getAddressLine2())
                .city(addressBody.getCity())
                .country(addressBody.getCountry())
                .build();

        address.setLocalUser(localUser);


        return addressRepository.save(address);
    }
}
