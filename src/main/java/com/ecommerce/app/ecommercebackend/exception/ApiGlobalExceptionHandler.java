package com.ecommerce.app.ecommercebackend.exception;

import com.ecommerce.app.ecommercebackend.api.dto.auth.LoginResponse;
import com.ecommerce.app.ecommercebackend.api.dto.auth.RegistrationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiGlobalExceptionHandler {

    @ExceptionHandler(ApiResponseFailureException.class)
    public ResponseEntity<ApiResponseError> handleApiResponseFailureException(ApiResponseFailureException ex){
        ApiResponseError errorResponse = new ApiResponseError(ex.getFailureType().getHttpStatus(), ex.getFailureType().getErrorMessage(), ex.getDetail());
        return ResponseEntity.status(errorResponse.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(InvalidJWTException.class)
    public ResponseEntity handleInvalidJWTException(InvalidJWTException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<RegistrationResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
        RegistrationResponse registrationResponse = RegistrationResponse.builder()
                .success(false)
                .detail("This account already exists, try using different credentials")
                .httpStatus(HttpStatus.CONFLICT)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(registrationResponse);
    }

    @ExceptionHandler(EmailFailureException.class)
    public ResponseEntity<RegistrationResponse> handleEmailFailureException(EmailFailureException ex){
        RegistrationResponse registrationResponse = RegistrationResponse.builder()
                .success(false)
                .detail("Email could not be sent, try again later")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registrationResponse);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<LoginResponse> handleUserNotVerifiedException(UserNotVerifiedException ex){

        String failureReason = "User is not verified";
        if(ex.isNewEmailSent()){
            failureReason += ", the email has been resent";
        }

        LoginResponse loginResponse = LoginResponse.builder()
                .success(false)
                .detail(failureReason)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loginResponse);
    }

    @ExceptionHandler(UserBadCredentialsException.class)
    public ResponseEntity<LoginResponse> handleUserBadCredentialsException(UserBadCredentialsException ex){
        LoginResponse loginResponse = LoginResponse.builder()
                .detail(ex.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        ApiResponseError errorResponse = ApiResponseError.builder()
                .errorMessage("Invalid request content")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
