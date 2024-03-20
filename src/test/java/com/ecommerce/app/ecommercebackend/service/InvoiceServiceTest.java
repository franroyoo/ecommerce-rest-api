package com.ecommerce.app.ecommercebackend.service;
import com.ecommerce.app.ecommercebackend.api.dto.invoice.InvoiceJsonBody;
import com.ecommerce.app.ecommercebackend.api.dto.invoice.ItemBody;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {
    @Autowired
    private InvoiceService invoiceService;

    @Test
    public void GivenInvoiceJsonBody_WhenGenerateInvoice_ThenReturnInvoicePdf(){

        InvoiceJsonBody invoiceJsonBody = InvoiceJsonBody.builder()
                .to("To")
                .notes("This is a note")
                .items(Arrays.asList(new ItemBody("name",5, 5L)))
                .shipTo("emailtoshipto@gmail.com")
                .number(5L)
                .build();

        Assertions.assertNotNull(invoiceService.generateInvoice(invoiceJsonBody));
        Assertions.assertDoesNotThrow(() -> ApiResponseFailureException.class);

    }
}
