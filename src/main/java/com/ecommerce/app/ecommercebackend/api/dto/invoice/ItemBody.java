package com.ecommerce.app.ecommercebackend.api.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ItemBody {
    private String name;
    private Integer quantity;
    private Long unit_cost;
}
