package com.ecommerce.app.ecommercebackend.exception;

public class UserBadCredentialsException extends RuntimeException {
    public UserBadCredentialsException(String msg) {
        super(msg);
    }
}
