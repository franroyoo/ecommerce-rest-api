package com.ecommerce.app.ecommercebackend.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String jwt;
    private boolean success;
    private String detail;

}
