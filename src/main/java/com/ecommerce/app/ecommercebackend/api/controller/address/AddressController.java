package com.ecommerce.app.ecommercebackend.api.controller.address;

import com.ecommerce.app.ecommercebackend.api.dto.AddressBody;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<Address> getAddresses(@AuthenticationPrincipal LocalUser localUser){
        return localUser.getAddresses();
    }

    @PostMapping("/new")
    public ResponseEntity<Address> addAddress(@AuthenticationPrincipal LocalUser localUser, @Valid @RequestBody AddressBody addressBody){
        Address address = addressService.addAddress(localUser, addressBody);
        return ResponseEntity.ok(address);
    }
}
