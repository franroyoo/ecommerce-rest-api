package com.ecommerce.app.ecommercebackend.api.controller.order;

import com.ecommerce.app.ecommercebackend.api.dto.OrderBody;
import com.ecommerce.app.ecommercebackend.exception.OrderDoesNotExistException;
import com.ecommerce.app.ecommercebackend.exception.OutOfStockException;
import com.ecommerce.app.ecommercebackend.exception.ProductDoesNotExistException;
import com.ecommerce.app.ecommercebackend.model.LocalUser;
import com.ecommerce.app.ecommercebackend.model.WebOrder;
import com.ecommerce.app.ecommercebackend.service.WebOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                        description = "Create order successfully",
                        content = @Content(mediaType = "application/json")
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Out of stock",
                        content = @Content
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Product does not exist",
                        content = @Content
                ),

                @ApiResponse(
                    responseCode = "403",
                        description = "User not authenticated",
                        content = @Content
                )
            }
    )
    @PostMapping("/new")
    public ResponseEntity createOrder(@AuthenticationPrincipal LocalUser user, @Valid @RequestBody OrderBody orderBody){

        try{
            WebOrder order = orderService.createOrder(orderBody, user);
            return ResponseEntity.ok(order);
        }catch (OutOfStockException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch (ProductDoesNotExistException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteOrder(@AuthenticationPrincipal LocalUser user, @PathVariable Long id){
        try{
            orderService.deleteOrder(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }catch (OrderDoesNotExistException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
