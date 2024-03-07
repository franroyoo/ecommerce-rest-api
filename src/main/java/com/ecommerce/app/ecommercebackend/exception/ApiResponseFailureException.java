package com.ecommerce.app.ecommercebackend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ApiResponseFailureException extends RuntimeException{

    private final HttpStatus httpStatus;

    public ApiResponseFailureException(HttpStatus httpStatus, String message){
        super(message);
        this.httpStatus = httpStatus;
    }
}
