package com.ecommerce.app.ecommercebackend.service;

import com.ecommerce.app.ecommercebackend.api.dto.invoice.InvoiceJsonBody;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;


@Service
public class InvoiceService {

    @Value("${invoice.api.uri}")
    private String URI;
    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] generateInvoice(InvoiceJsonBody invoiceJsonBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_PDF));

        HttpEntity<InvoiceJsonBody> invoiceBodyHttpEntity = new HttpEntity<>(invoiceJsonBody, headers);

        try{
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(URI, invoiceBodyHttpEntity, byte[].class);
            return responseEntity.getBody();
        }catch (Exception e){
            throw new ApiResponseFailureException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate invoice");
        }

    }
}
