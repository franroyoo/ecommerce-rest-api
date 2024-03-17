package com.ecommerce.app.ecommercebackend.api.controller.auth;

import com.ecommerce.app.ecommercebackend.api.dto.auth.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.auth.LoginResponse;
import com.ecommerce.app.ecommercebackend.api.dto.auth.RegistrationBody;
import com.ecommerce.app.ecommercebackend.api.dto.auth.RegistrationResponse;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseError;
import com.ecommerce.app.ecommercebackend.exception.EmailFailureException;
import com.ecommerce.app.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.app.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private UserService userService;

    @Autowired
    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register user")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User registered successfully",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "409",
                        description = "User already exists",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationResponse.class))}

                ),

                @ApiResponse(
                        responseCode = "500",
                        description = "Email service failed",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request body",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationBody registrationBody) throws EmailFailureException, UserAlreadyExistsException {
        userService.registerUser(registrationBody);
        RegistrationResponse registrationResponse = RegistrationResponse.builder()
                .success(true)
                .detail("Your registration has been processed successfully. Check your email to verify your account")
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(registrationResponse);
    }

    @Operation(summary = "Login user")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User logged in successfully",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "403",
                        description = "User not verified",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "500",
                        description = "Email service failed",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request body",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}
                ),

                @ApiResponse(
                        responseCode = "401",
                        description = "User or password not found",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}
                )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {

        String jwt = userService.loginUser(loginBody);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setJwt(jwt);
        loginResponse.setSuccess(true);
        loginResponse.setDetail("You have logged in successfully and you now have access to your JWT token");

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Get logged in user information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User information retrieved successfully",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LocalUser.class))}
                ),

                @ApiResponse(
                        responseCode = "403",
                        description = "Unauthorized",
                        content = @Content
                )
            }
    )
    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user){
        return user;
    }

    @Operation(summary = "Verify user email")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User email verified successfully",
                        content = @Content
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "User already verified",
                        content = @Content
                )
            }
    )
    @PostMapping("/verify")
    public ResponseEntity verifyEmail(@RequestParam String token){
        if (userService.verifyUser(token)){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
