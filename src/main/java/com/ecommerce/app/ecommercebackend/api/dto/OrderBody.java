package com.ecommerce.app.ecommercebackend.api.dto;

import com.ecommerce.app.ecommercebackend.model.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBody {

    @NotNull
    private List<ProductBody> products;

    @NotNull
    @NotBlank
    private String address_line_1;
}
