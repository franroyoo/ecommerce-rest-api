package com.ecommerce.app.ecommercebackend.api.dto.auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {

    private boolean success;
    private String detail;
    @JsonProperty("http_status")
    private HttpStatus httpStatus;
}
