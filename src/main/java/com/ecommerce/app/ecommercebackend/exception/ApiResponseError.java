package com.ecommerce.app.ecommercebackend.exception;

import com.ecommerce.app.ecommercebackend.validation.FailureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class ApiResponseError {

    @JsonProperty("http_status")
    private HttpStatus httpStatus;

    @JsonProperty("message")
    private String errorMessage;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String detail;
}
