package com.ecommerce.app.ecommercebackend.exception;

import com.ecommerce.app.ecommercebackend.validation.FailureType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ApiResponseFailureException extends RuntimeException{

    private final FailureType failureType;

    private String detail;

    public ApiResponseFailureException(FailureType failureType, String detail){
        this.failureType = failureType;
        this.detail = detail;
    }
}
