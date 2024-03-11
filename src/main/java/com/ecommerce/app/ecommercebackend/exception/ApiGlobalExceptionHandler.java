package com.ecommerce.app.ecommercebackend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiGlobalExceptionHandler {

    @ExceptionHandler(ApiResponseFailureException.class)
    public ResponseEntity<ApiResponseError> handleApiResponseFailureException(ApiResponseFailureException ex){
        ApiResponseError errorResponse = new ApiResponseError(ex.getFailureType().getHttpStatus(), ex.getFailureType().getErrorMessage(), ex.getDetail());
        return ResponseEntity.status(errorResponse.getHttpStatus()).body(errorResponse);
    }
}
