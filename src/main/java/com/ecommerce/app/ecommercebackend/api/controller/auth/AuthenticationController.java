package com.ecommerce.app.ecommercebackend.api.controller.auth;

import com.ecommerce.app.ecommercebackend.api.dto.LoginBody;
import com.ecommerce.app.ecommercebackend.api.dto.LoginResponse;
import com.ecommerce.app.ecommercebackend.api.dto.RegistrationBody;
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
import org.springframework.security.core.userdetails.UserDetails;
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
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "409",
                        description = "User already exists",
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "500",
                        description = "Email service failed",
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request body",
                        content = @Content
                )
            }
    )
    @PostMapping("/register")
    public ResponseEntity registerUser(@Valid @RequestBody RegistrationBody registrationBody){
        try{
            userService.registerUser(registrationBody);
            return ResponseEntity.ok().build();
        }catch(UserAlreadyExistsException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "500",
                        description = "Email service failed",
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request body",
                        content = @Content
                ),

                @ApiResponse(
                        responseCode = "401",
                        description = "User not found",
                        content = @Content
                )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody){
        String jwt = null;
        try {
            jwt = userService.loginUser(loginBody);
        } catch (UserNotVerifiedException ex) {

            LoginResponse response = new LoginResponse();
            String reason = "USER_NOT_VERIFIED";
            if (ex.isNewEmailSent()){
                reason += "_EMAIL_RESENT";
            }

            response.setSuccess(false);
            response.setFailureReason(reason);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (jwt == null){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
       }else {
           LoginResponse loginResponse = new LoginResponse();
           loginResponse.setJwt(jwt);
            loginResponse.setSuccess(true);
           return ResponseEntity.ok(loginResponse);
       }

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
