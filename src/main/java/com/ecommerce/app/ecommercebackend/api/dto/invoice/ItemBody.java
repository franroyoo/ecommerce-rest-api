package com.ecommerce.app.ecommercebackend.api.dto.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ItemBody {
    private String name;
    private Integer quantity;

    @JsonProperty("unit_cost")
    private Long unitCost;
}
