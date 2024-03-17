package com.ecommerce.app.ecommercebackend.api.controller.address;

import com.ecommerce.app.ecommercebackend.api.dto.AddressBody;
import com.ecommerce.app.ecommercebackend.model.Address;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get user addresses")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Addresses retrieved successfully",
                            content = @Content
                    )
            }
    )
    @GetMapping
    public List<Address> getAddresses(@AuthenticationPrincipal LocalUser localUser){
        return localUser.getAddresses();
    }

    @Operation(summary = "Add address to user account")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Address added successfully",
                            content = @Content
                    )
            }
    )
    @PostMapping("/new")
    public ResponseEntity<Address> addAddress(@AuthenticationPrincipal LocalUser localUser, @Valid @RequestBody AddressBody addressBody){
        Address address = addressService.addAddress(localUser, addressBody);
        return ResponseEntity.ok(address);
    }
}
