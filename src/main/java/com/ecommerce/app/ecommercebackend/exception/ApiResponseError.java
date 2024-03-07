package com.ecommerce.app.ecommercebackend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiResponseError {
    private String message;
    private HttpStatus httpStatus;
}
