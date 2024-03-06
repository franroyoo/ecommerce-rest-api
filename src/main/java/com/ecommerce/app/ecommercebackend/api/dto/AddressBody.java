package com.ecommerce.app.ecommercebackend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressBody {

    @NotNull
    @NotBlank
    @Size(min=4, max=50)
    private String addressLine1;

    @NotNull
    @NotBlank
    @Size(min=4, max=50)
    private String addressLine2;

    @NotNull
    @NotBlank
    @Size(min=4, max=40)
    private String city;
    @NotNull
    @NotBlank
    @Size(min=4, max=25)
    private String country;
}
