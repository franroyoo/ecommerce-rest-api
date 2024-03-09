package com.ecommerce.app.ecommercebackend.api.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InvoiceJsonBody {

    private static final String from = "Francisco Royo";
    private String to;
    private Long number;
    private List<ItemBody> items;
    private String notes;
    private String ship_to;
}
