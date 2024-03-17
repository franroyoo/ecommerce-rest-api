package com.ecommerce.app.ecommercebackend.api.controller.order;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.exception.*;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.service.WebOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class WebOrderController {
    private WebOrderService orderService;

    @Autowired
    public WebOrderController(WebOrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get order list")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Get order list successfully",
                        content = @Content(mediaType = "application/json")
                )
            }
    )
    @GetMapping("/list")
    public List<WebOrder> getOrderList(@AuthenticationPrincipal LocalUser user){
        return orderService.getOrderList(user);
    }

    @Operation(summary = "Create order")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Order created successfully and invoice generated",
                        content = @Content(mediaType = "application/pdf")
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Out of stock",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "Product not found or Address not found",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                ),

                @ApiResponse(
                    responseCode = "403",
                        description = "User not authenticated",
                        content = @Content
                )
            }
    )
    @PostMapping("/new")
    public ResponseEntity<byte[]> createOrder(@AuthenticationPrincipal LocalUser user, @Valid @RequestBody OrderBody orderBody){

        byte[] orderPdf = orderService.createOrder(orderBody, user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(orderPdf);
    }

    @Operation(summary = "Delete order")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order deleted successfully",
                            content = @Content(mediaType = "application/json")
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseError.class))}
                    ),

                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authenticated",
                            content = @Content
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOrder(@AuthenticationPrincipal LocalUser user, @PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
}
