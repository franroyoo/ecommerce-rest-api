package com.ecommerce.app.ecommercebackend.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FailureType {

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "Out of stock"),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),

    INVOICE_GENERATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Invoice could not be generated"),

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address not found"),

    USER_AUTHORITY_EXISTS(HttpStatus.BAD_REQUEST, "User authority exists"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found");

    @JsonProperty("http_status")
    private final HttpStatus httpStatus;
    @JsonProperty("message")
    private final String errorMessage;

    FailureType(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }
}
