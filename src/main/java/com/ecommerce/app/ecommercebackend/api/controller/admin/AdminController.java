package com.ecommerce.app.ecommercebackend.api.controller.admin;

import com.ecommerce.app.ecommercebackend.api.dto.auth.LoginResponse;
import com.ecommerce.app.ecommercebackend.exception.ApiResponseError;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Add premium role to user")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Premium role added successfully",
                            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LocalUser.class))}
                    ),

                    @ApiResponse(
                            responseCode = "403",
                            description = "Unauthorized",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User already has premium role",
                            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<LocalUser> promoteUserToPremium(@PathVariable Long id){
        LocalUser user = adminService.promoteUserToPremium(id);
        return ResponseEntity.ok(user);
    }
}
